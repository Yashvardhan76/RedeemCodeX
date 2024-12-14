package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.models.RedeemCode
import org.bukkit.entity.Player

class CodeValidation(val plugin: RedeemX, private val userCode: String) {
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

    fun isReachedMaximumRedeem(): Boolean {
        if (code.maxRedeems <= 0) return false
        return code.usage.size >= code.maxRedeems
    }

    fun isReachedMaximumPlayer(): Boolean {
        if (code.maxPlayers <= 0) return false
        return code.usage.size >= code.maxPlayers
    }

    fun isCodeEnabled(): Boolean {
        return code.isEnabled
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
        if (isPinRequired()) return true
        return code.pin == pin
    }

    fun isTargetRequired(): Boolean {
        return code.target.isNotEmpty()
    }

    fun isTargetValid(player: String): Boolean {
        if (!isTargetRequired()) return true
        return code.target.contains(player)
    }

}