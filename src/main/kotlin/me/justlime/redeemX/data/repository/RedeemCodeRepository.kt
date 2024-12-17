package me.justlime.redeemX.data.repository

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.local.RedeemCodeDao
import me.justlime.redeemX.enums.JProperty
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.models.RedeemTemplate
import me.justlime.redeemX.utilities.RedeemCodeService

/**
 * This repository class is responsible for managing redeem codes from database.
 */
class RedeemCodeRepository(plugin: RedeemX) {
    private val redeemCodeDao: RedeemCodeDao = plugin.redeemCodeDB
    private val service = RedeemCodeService()

    fun getCode(code: String): RedeemCode? {
        return redeemCodeDao.get(code)
    }

    /**Used this for getting list of cached Code useful for listing**/
    fun getCachedCode(): List<String> {
        return redeemCodeDao.getCachedCodes()
    }

    //TODO This Performance is currently heavy
    fun getCodesByProperty(property: JProperty, value: String): List<RedeemCode> {
        return redeemCodeDao.getByProperty(property, value)
    }


    fun getAllCodes(): List<RedeemCode> {
        return redeemCodeDao.getEntireCodes()
    }

    /**
     * Toggles the enabled state of a redeem code.
     *
     * @param redeemCode The redeem code to toggle.
     * @return `true` if the operation was successful, `false` otherwise.
     */
    fun toggleEnabled(redeemCode: RedeemCode): Boolean {
        redeemCode.enabled = !redeemCode.enabled
        return true
    }

    /**
     * Sets the maximum number of redemptions for a redeem code.
     *
     * @param redeemCode The redeem code to modify.
     * @param maxRedeems The new maximum number of redemptions.
     * @return `true` if the operation was successful, `false` otherwise.
     * **/
    fun setMaxRedeems(redeemCode: RedeemCode, maxRedeems: Int): Boolean {
        redeemCode.redemption = maxRedeems
        return true
    }

    /**
     * Sets the maximum number of players who can redeem a redeem code.
     *
     * @param redeemCode The redeem code to modify.
     * @param maxPlayers The new maximum number of players. Set to `0` or less for no limit.
     * @return `true` if the operation was successful, `false` otherwise.
     */
    fun setMaxPlayers(redeemCode: RedeemCode, maxPlayers: Int): Boolean {
        redeemCode.limit = maxPlayers
        return true
    }

    fun setPermission(redeemCode: RedeemCode, permission: String): Boolean {
        redeemCode.permission = permission.replace("{code}",redeemCode.code)
        return true
    }

    fun setTemplate(redeemCode: RedeemCode, template: RedeemTemplate): Boolean {

        if (template.name.isBlank()) redeemCode.template = ""
        redeemCode.apply {
            this.template = template.name
            redemption = template.maxRedeems
            limit = template.maxPlayers
            permission = template.permissionValue
            permission = if (template.permissionRequired) template.permissionValue else ""
            pin = template.pin
            validFrom = service.currentTime
            duration = template.duration
            usedBy = mutableMapOf()
            target = mutableListOf()
            commands = template.commands
            locked = true
            lastRedeemed = mutableMapOf()
            cooldown = template.cooldown
        }
        return true
    }

    fun setTemplateLocked(redeemCode: RedeemCode, templateLocked: Boolean): Boolean {
        redeemCode.locked = templateLocked
        return true
    }

    fun addTarget(redeemCode: RedeemCode, target: String): Boolean {
        if (target.isBlank()) return false
        redeemCode.target.add(target)
        return true
    }

    fun setTarget(redeemCode: RedeemCode, target: List<String>): Boolean {
        redeemCode.target = target.distinct().toMutableList()
        return true
    }

    fun removeTarget(redeemCode: RedeemCode, target: String): Boolean {
        if (target.isBlank()) return false
        redeemCode.target.remove(target)
        return true
    }

    fun clearTarget(redeemCode: RedeemCode): Boolean {
        redeemCode.target.clear()
        return true
    }

    fun clearUsage(redeemCode: RedeemCode): Boolean {
        redeemCode.usedBy = mutableMapOf()
        return true
    }

    fun setCommands(redeemCode: RedeemCode, commands: MutableMap<Int, String>): Boolean {
        redeemCode.commands = commands
        return true
    }

    fun addCommand(redeemCode: RedeemCode, command: String): Boolean {
        if (command.isBlank()) return false
        val id = redeemCode.commands.keys.maxOrNull() ?: 0
        redeemCode.commands[id + 1] = command
        return true
    }

    fun removeCommand(redeemCode: RedeemCode, id: Int): Boolean {
        redeemCode.commands.remove(id)
        return true
    }

    fun clearCommands(redeemCode: RedeemCode): Boolean {
        redeemCode.commands.clear()
        return true
    }

    fun setStoredTime(redeemCode: RedeemCode): Boolean {
        redeemCode.validFrom = service.currentTime
        return true
    }

    fun addDuration(code: RedeemCode, duration: String): Boolean {
        if (!service.isDurationValid(duration)) return false
        val existingDuration = code.duration
        code.duration = service.adjustDuration(existingDuration, duration, isAdding = true)
        return true
    }

    fun removeDuration(code: RedeemCode, duration: String): Boolean {
        if (!service.isDurationValid(duration)) return false
        val existingDuration = code.duration
        code.duration = service.adjustDuration(existingDuration, duration, isAdding = false)
        return true
    }

    fun setDuration(code: RedeemCode, duration: String): Boolean {
        if (!service.isDurationValid(duration)) return false
        code.duration = duration
        return true
    }

    fun clearDuration(code: RedeemCode): Boolean {
        code.duration = "0s"
        return true
    }

    fun setLastRedeemedTime(redeemCode: RedeemCode,player: String): Boolean {
        redeemCode.lastRedeemed[player] = service.currentTime
        return true
    }

    fun setCooldown(redeemCode: RedeemCode, cooldown: String): Boolean {
        redeemCode.cooldown = cooldown
        if (cooldown.isBlank()) redeemCode.cooldown = ""
        return true
    }

    fun setPin(redeemCode: RedeemCode, pin: Int): Boolean {
        redeemCode.pin = pin
        return true
    }

    fun removePin(redeemCode: RedeemCode): Boolean {
        setPin(redeemCode, 0)
        return true
    }

    fun upsertCode(redeemCode: RedeemCode): Boolean {
        return redeemCodeDao.upsertCode(redeemCode)
    }

    fun upsertCodes(redeemCodes: List<RedeemCode>): Boolean {
        return redeemCodeDao.upsertCodes(redeemCodes)
    }

    fun deleteCode(code: String): Boolean {
        return redeemCodeDao.deleteByCode(code)
    }

    fun deleteEntireCodes(): Boolean {
        return redeemCodeDao.deleteEntireCodes()
    }

    private inline fun filterCodes(predicate: (RedeemCode) -> Boolean): List<RedeemCode> {
        return getAllCodes().filter(predicate)
    }

}