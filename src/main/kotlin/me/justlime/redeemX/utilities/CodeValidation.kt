package me.justlime.redeemX.utilities

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CodeValidation(val plugin: RedeemX, private val userCode: String, private val sender: CommandSender) {
    private val repo = RedeemCodeRepository(plugin)
    lateinit var redeemCode: RedeemCode

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
                val elapsedTimeInSeconds = (lastRedeemedTime / 1000) + JService.parseDurationToSeconds(redeemCode.cooldown) - (currentTimeMillis / 1000)
                val duration = JService.adjustDuration(JService.formatSecondsToDuration(elapsedTimeInSeconds), "0s", true)
                placeHolder.cooldown = duration
            }
            return true
        }
        redeemCode.lastRedeemed[sender.name] = JService.getCurrentTime()

        return false
    }

}