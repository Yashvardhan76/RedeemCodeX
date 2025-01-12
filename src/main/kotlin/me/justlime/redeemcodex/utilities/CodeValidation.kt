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
        val COOLDOWN_KEY = NamespacedKey(plugin, "cooldown")
        val cooldown = plugin.configRepo.getConfigValue("redeem-command.cooldown")
        if(sender !is Player) return false

        fun getLastRedeemed(): Long {
            val container = sender.persistentDataContainer
            return  container.get(COOLDOWN_KEY, PersistentDataType.LONG) ?: 0L
        }
        fun setLastRedeemed(timestamp: Long) {
            val container = sender.persistentDataContainer
            container.set(COOLDOWN_KEY, PersistentDataType.LONG, timestamp)
        }
        val onCooldown: Boolean = JService.onCoolDown(cooldown, mutableMapOf(sender.name to Timestamp(getLastRedeemed())), sender.name)
        if (!onCooldown){ setLastRedeemed(JService.getCurrentTime().time) }
        return onCooldown
    }

    private fun isValidCode(code: String): Boolean {
        return code.matches(Regex("^[a-zA-Z0-9]{1,100}$"))
    }

    fun isCodeExist(): Boolean {
        if (!isValidCode(userCode)) return false
        redeemCode = repo.getCode(this.userCode) ?: return false
        return true
    }

    fun isReachedMaximumRedeem(sender: CommandSender): Boolean {
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
        return redeemCode.pin > 0
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
        if (JService.onCoolDown(redeemCode.cooldown, redeemCode.lastRedeemed, sender.name)) {
            val lastRedeemedTime = redeemCode.lastRedeemed[sender.name]?.time
            if (lastRedeemedTime != null) {
                val currentTimeMillis = JService.getCurrentTime().time
                val elapsedTimeInSeconds =
                    (lastRedeemedTime / 1000) + JService.parseDurationToSeconds(redeemCode.cooldown) - (currentTimeMillis / 1000)
                val duration = JService.adjustDuration(JService.formatSecondsToDuration(elapsedTimeInSeconds), "0s", true)
                placeHolder.cooldown = duration
            }
            return true
        }
        redeemCode.lastRedeemed[sender.name] = JService.getCurrentTime()

        return false
    }

}