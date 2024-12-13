package me.justlime.redeemX.data.repository

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.local.RedeemCodeDao
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.utilities.RedeemCodeService
import java.sql.Timestamp

/**
 * This repository class is responsible for managing redeem codes from database.
 */
class RedeemCodeRepository(plugin: RedeemX) {
    private val redeemCodeDao: RedeemCodeDao = plugin.redeemCodeDB
    private val service = RedeemCodeService()

    fun getCode(code: String): RedeemCode? {
        return redeemCodeDao.get(code)
    }

    /**Used this for getting list of cached Code use full for listing**/
    fun getCachedCode(): List<String> {
        return redeemCodeDao.getCachedCodes()
    }

    fun getCodesByTemplate(template: String): List<RedeemCode> {
        return redeemCodeDao.getTemplateCodes(template)
    }

    /**Note: Heavy Function Depends on Database **/
    fun getCodesByExpired(expired: Boolean = true): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach {
            if (service.isExpired(it) == expired) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByMaxRedeem(maxRedeem: Int): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach {
            if (it.maxRedeems == maxRedeem) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByMaxPlayer(maxPlayer: Int): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach {
            if (it.maxPlayers == maxPlayer) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByEnabled(enabled: Boolean): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach {
            if (it.isEnabled == enabled) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByPermission(permission: String): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach {
            if (it.permission == permission) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByDuration(duration: String): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()

        getEntireCodes().forEach {
            val convertedDuration = service.convertDurationToSeconds(duration)
            if (it.duration == convertedDuration) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByPin(pin: Int): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach {
            if (it.pin == pin) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByTemplateLocked(templateLocked: Boolean = true): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach {
            if (it.templateLocked == templateLocked) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByCooldown(cooldown: String): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach {
            if (it.cooldown == cooldown) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getEntireCodes(): List<RedeemCode> {
        return redeemCodeDao.getEntireCodes()
    }

    fun addDuration(code: RedeemCode, duration: String): Boolean {
        if(!service.isDurationValid(duration)) return false
        val existingDuration = code.duration ?: "0s"
        val durationValue = service.adjustDuration(existingDuration, duration, isAdding = true).toString() + 's'
        code.duration = service.adjustDuration(existingDuration, durationValue, isAdding = true).toString() + 's'
        return true
    }

    fun removeDuration(code: RedeemCode, duration: String): Boolean{
        if(!service.isDurationValid(duration)) return false
        val existingDuration = code.duration ?: "0s"
        val durationValue = service.adjustDuration(existingDuration, duration, isAdding = false).toString() + 's'
        code.duration = if ((durationValue.dropLast(1).toIntOrNull() ?: -1) < 0) null else durationValue
        return true
    }

    fun setDuration(code: RedeemCode, duration: String): Boolean{
        if(!service.isDurationValid(duration)) return false
        code.duration = duration
        return true
    }

    fun clearDuration(code: RedeemCode): Boolean{
        code.duration = "0s"
        return true
    }

    fun toggleEnabled(redeemCode: RedeemCode): Boolean {
        redeemCode.isEnabled = !redeemCode.isEnabled
        return true
    }

    fun setMaxRedeems(redeemCode: RedeemCode, maxRedeems: Int): Boolean {
        if (maxRedeems < 1) return false
        redeemCode.maxRedeems = maxRedeems
        return true
    }

    fun setMaxPlayers(redeemCode: RedeemCode, maxPlayers: Int): Boolean {
        if (maxPlayers < 1) return false
        redeemCode.maxPlayers = maxPlayers
        return true
    }

    fun setPermission(redeemCode: RedeemCode, permission: String): Boolean {
        redeemCode.permission = permission
        if (permission.isBlank()) redeemCode.permission = null
        return true
    }

    fun setTemplate(redeemCode: RedeemCode, template: String): Boolean {
        redeemCode.template = template
        if (template.isBlank()) redeemCode.template = ""
        return true
    }

    fun setTemplateLocked(redeemCode: RedeemCode, templateLocked: Boolean): Boolean {
        redeemCode.templateLocked = templateLocked
        return true
    }

    fun setCooldown(redeemCode: RedeemCode, cooldown: String): Boolean {
        redeemCode.cooldown = cooldown
        if (cooldown.isBlank()) redeemCode.cooldown = null
        return true
    }

    fun addTarget(redeemCode: RedeemCode,target: String): Boolean{
        if(target.isBlank()) return false
        redeemCode.target.add(target)
        return true
    }

    fun setTarget(redeemCode: RedeemCode, target: List<String>): Boolean {
        redeemCode.target = target.distinct().toMutableList()
        return true
    }

    fun removeTarget(redeemCode: RedeemCode,target: String): Boolean{
        if(target.isBlank()) return false
        redeemCode.target.remove(target)
        return true
    }

    fun clearTarget(redeemCode: RedeemCode): Boolean{
        redeemCode.target.clear()
        return true
    }

    fun setUsage(redeemCode: RedeemCode, usage: MutableMap<String, Int>): Boolean {
        //TODO
        return true
    }

    fun addCommand(redeemCode: RedeemCode,command: String): Boolean{
        if(command.isBlank()) return false
        val id = redeemCode.commands.keys.maxOrNull() ?: 0
        redeemCode.commands[id + 1] = command
        return true
    }

    fun removeCommand(redeemCode: RedeemCode,id: Int): Boolean{
        redeemCode.commands.remove(id)
        return true
    }

    fun setCommands(redeemCode: RedeemCode, commands: MutableMap<Int, String>): Boolean {
        redeemCode.commands = commands
        return true
    }

    fun clearCommands(redeemCode: RedeemCode): Boolean{
        redeemCode.commands.clear()
        return true
    }


    fun setStoredTime(redeemCode: RedeemCode): Boolean {
        redeemCode.storedTime = Timestamp.valueOf(service.currentTime)
        return true
    }

    fun setStoredCooldown(redeemCode: RedeemCode): Boolean {
        redeemCode.storedCooldown = Timestamp.valueOf(service.currentTime)
        return true
    }

    fun setPin(redeemCode: RedeemCode, pin: Int): Boolean {
        redeemCode.pin = pin
        return true
    }

    fun removePin(redeemCode: RedeemCode): Boolean{
        setPin(redeemCode,0)
        return true
    }

    fun upsertCode(redeemCode: RedeemCode): Boolean {
        return redeemCodeDao.upsert(redeemCode)
    }

    fun deleteCode(code: String): Boolean {
        return redeemCodeDao.deleteByCode(code)
    }

    fun deleteEntireCodes(): Boolean {
        return redeemCodeDao.deleteEntireCodes()
    }

}