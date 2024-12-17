package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CodeValidation(val plugin: RedeemX, private val userCode: String,private val sender: CommandSender) {
    private val service = plugin.service
    private val repo = RedeemCodeRepository(plugin)
    lateinit var code: RedeemCode

    fun isValidCode(code: String): Boolean {
        return code.matches(Regex("^[a-zA-Z0-9]{4,100}$"))
    }

    fun isCodeExist(): Boolean {
        if (!isValidCode(userCode)) return false
        code = repo.getCode(this.userCode) ?: return false
        return true
    }

    fun isReachedMaximumRedeem(sender: CommandSender): Boolean {
        if (code.redemption <= 0) return false
        return (code.usedBy[sender.name] ?: 0) > code.redemption
    }

    fun isReachedMaximumPlayer(): Boolean {
        if (code.limit <= 0) return false
        return code.usedBy.size > code.limit
    }

    fun isCodeEnabled(): Boolean {
        return code.enabled
    }

    fun requiredPermission(player: Player): Boolean{
        return code.permission.isNotBlank()
    }

    fun hasPermission(player: Player): Boolean {
        if (!requiredPermission(player)) return true
        return player.hasPermission(code.permission)
    }

    fun isCodeExpired(): Boolean {
        if (code.duration.isBlank()) return false
        if (code.duration == "0s") return false
        return service.isExpired(code)
    }

    fun isPinRequired(): Boolean{
        return code.pin > 0
    }


    fun isCorrectPin(pin: Int): Boolean {
        return code.pin == pin
    }

    fun isTargetRequired(): Boolean {
        return code.target.isNotEmpty()
    }

    fun isTargetValid(player: String): Boolean {
        if (!isTargetRequired()) return true
        return code.target.contains(player)
    }

    fun isCooldown(placeHolder: CodePlaceHolder): Boolean{
        if (service.onCoolDown(code.cooldown,code.lastRedeemed,sender.name)) {
            val lastRedeemedTime = code.lastRedeemed[sender.name]?.time
            if (lastRedeemedTime != null) {
                val currentTimeMillis = service.getCurrentTime().time
                val elapsedTimeInSeconds = (lastRedeemedTime / 1000) + service.parseDurationToSeconds(code.cooldown) - (currentTimeMillis / 1000)
                val duration = service.adjustDuration(service.formatSecondsToDuration(elapsedTimeInSeconds),"0s", true)
                placeHolder.cooldown = duration
            }
            return true
        }
        repo.setLastRedeemedTime(code,sender.name)
        return false
    }

}