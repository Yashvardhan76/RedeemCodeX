/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 *
 */

package me.justlime.redeemcodex.utilities

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.data.repository.RedeemCodeRepository
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.models.RedeemCode
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.sql.Timestamp

class CodeValidation(val plugin: RedeemCodeX, private val userCode: String, private val sender: CommandSender) {
    private val repo = RedeemCodeRepository(plugin)

    lateinit var redeemCode: RedeemCode

    fun isPlayerOnCooldown(): Boolean {
        val cooldownKey = NamespacedKey(plugin, "cooldown")
        val cooldown = plugin.configRepo.getConfigValue("redeem-command.cooldown")
        if (sender !is Player) return false

        fun getLastRedeemed(): Long {
            val container = sender.persistentDataContainer
            return container.get(cooldownKey, PersistentDataType.LONG) ?: 0L
        }

        fun setLastRedeemed(timestamp: Long) {
            val container = sender.persistentDataContainer
            container.set(cooldownKey, PersistentDataType.LONG, timestamp)
        }

        val onCooldown: Boolean = JService.onCoolDown(cooldown, mutableMapOf(sender.name to Timestamp(getLastRedeemed())), sender.name)
        if (!onCooldown) {
            setLastRedeemed(JService.getCurrentTime().time)
        }
        return onCooldown
    }

    private fun isValidCode(code: String): Boolean {
        return code.matches(Regex("^[a-zA-Z0-9_-]{1,100}$"))
    }

    fun isCodeExist(): Boolean {
        if (!isValidCode(userCode)) return false
        redeemCode = repo.getCode(this.userCode) ?: return false
        return true
    }

    fun isReachedMaximumRedeem(sender: Player): Boolean {
        if (!isIpEligible(sender, redeemCode)) return true
        if (redeemCode.redemption <= 0) return false
        return (redeemCode.usedBy[sender.name] ?: 0) >= redeemCode.redemption
    }

    fun isReachedMaximumPlayer(): Boolean {
        return if (redeemCode.usedBy[sender.name] != null) false
        else if (redeemCode.playerLimit <= 0) false
        else redeemCode.usedBy.size >= redeemCode.playerLimit
    }

    fun isCodeEnabled(): Boolean {
        return redeemCode.enabledStatus
    }

    private fun requiredPermission(): Boolean {
        return redeemCode.permission.isNotBlank()
    }

    fun hasPermission(player: Player): Boolean {
        if (!requiredPermission()) return true
        return player.hasPermission(redeemCode.permission)
    }

    fun isCodeExpired(): Boolean {
        if (redeemCode.duration.isBlank()) return false
        if (redeemCode.duration == "0s") return false
        return JService.isExpired(redeemCode)
    }

    fun isPinRequired(): Boolean {
        return redeemCode.pin.takeIf { it > 0 } != null
    }

    fun isCorrectPin(pin: Int): Boolean {
        return redeemCode.pin == pin
    }

    private fun isTargetRequired(): Boolean {
        return redeemCode.target.isNotEmpty()
    }

    fun isTargetValid(player: String): Boolean {
        if (!isTargetRequired()) return true
        return redeemCode.target.contains(player)
    }

    fun isCooldown(placeHolder: CodePlaceHolder): Boolean {
        val lastRedeemedTime = redeemCode.lastRedeemed[sender.name]?.time ?: return false
        val currentTimeMillis = JService.getCurrentTime().time
        val cooldownSeconds = JService.parseDurationToSeconds(redeemCode.cooldown)

        val elapsedTimeInSeconds = (lastRedeemedTime / 1000) + cooldownSeconds - (currentTimeMillis / 1000)

        return if (elapsedTimeInSeconds > 0) {
            placeHolder.cooldown = JService.adjustDuration(JService.formatSecondsToDuration(elapsedTimeInSeconds), "0s", true)
            true
        } else {
            redeemCode.lastRedeemed[sender.name] = JService.getCurrentTime()
            false
        }
    }

    private fun isIpEligible(player: Player, code: RedeemCode): Boolean {
        return true
        val playerIpAddress = player.address?.address?.hostAddress ?: return false

        // Get all accounts that have used this IP
        val accountsOnThisIp = code.playerIp.filter { it.key == playerIpAddress }.values

        // No previous usage, allow redemption
        if (accountsOnThisIp.isEmpty()) return true

        // Sum total redemptions across all accounts with this IP
        val totalRedemptions = accountsOnThisIp.sumOf { code.usedBy[it] ?: 0 }

        // Return true only if total redemptions are within limit
        return totalRedemptions < code.redemption
    }
}