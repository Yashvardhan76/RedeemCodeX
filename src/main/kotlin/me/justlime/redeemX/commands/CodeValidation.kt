package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import org.bukkit.entity.Player

class CodeValidation(val plugin: RedeemX, private val userCode: String) {
    private val service = plugin.service
    private val repo = RedeemCodeRepository(plugin)
    val code = repo.getCode(this.userCode)

    fun isValidCode(code: String): Boolean {
        return code.matches(Regex("^[a-zA-Z0-9]{4,10}$"))
    }

    fun isCodeExist(): Boolean {
        return code != null
    }

    fun isReachedMaximumRedeem(): Boolean {
        if (code == null) return false
        return code.usage.size >= code.maxRedeems
    }

    fun isReachedMaximumPlayer(): Boolean {
        if (code == null) return false
        return code.usage.size >= code.maxPlayers
    }

    fun isCodeEnabled(): Boolean {
        if (code == null) return false
        return code?.isEnabled ?: false
    }

    fun hasPermission(player: Player): Boolean {
        if (code == null) return false
        if (code.permission == null) return true
        return !code.permission.isNullOrBlank() && player.hasPermission(code.permission!!)
    }

    fun isCodeExpired(): Boolean {
        if (code == null) return false
        return service.isExpired(code)
    }

    fun isCorrectPin(pin: Int): Boolean {
        if (code == null) return false
        return code.pin == pin
    }

    fun isTargetValid(player: String): Boolean {
        if (code == null) return false
        return code.target.contains(player)
    }

}