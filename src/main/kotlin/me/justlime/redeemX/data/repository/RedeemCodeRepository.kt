package me.justlime.redeemX.data.repository

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.local.RedeemCodeDao
import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.utilities.RedeemCodeService

/**
 * This repository class is responsible for managing redeem codes from database.
 */
class RedeemCodeRepository(private val plugin: RedeemX){
    private val redeemCodeDao: RedeemCodeDao = plugin.redeemCodeDB
    private val service = RedeemCodeService(plugin)

    fun getCode(code: String): RedeemCode {
        return redeemCodeDao.get(code) ?: throw Exception("Code not found")
    }

    fun getCodesByTemplate(template: String): List<RedeemCode> {
        return redeemCodeDao.getTemplateCodes(template)
    }

    /**Note: Heavy Function Depends on Database **/
    fun getCodesByExpired(expired: Boolean = true): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach{
                if (service.isExpired(it.code) == expired) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByMaxRedeem(maxRedeem: Int): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach{
            if (it.maxRedeems == maxRedeem) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByMaxPlayer(maxPlayer: Int): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach{
            if (it.maxPlayers == maxPlayer) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByEnabled(enabled: Boolean): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach{
            if (it.isEnabled == enabled) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByPermission(permission: String): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach{
            if (it.permission == permission) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByDuration(duration: String): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()

        getEntireCodes().forEach{
            val convertedDuration = service.convertDurationToSeconds(duration)
            if (it.duration == convertedDuration) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByPin(pin: Int): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach{
            if (it.pin == pin) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByTemplateLocked(templateLocked: Boolean = true): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach{
            if (it.templateLocked == templateLocked) redeemCodeList.add(it)
        }
        return redeemCodeList
    }

    fun getCodesByCooldown(cooldown: String): List<RedeemCode> {
        val redeemCodeList = mutableListOf<RedeemCode>()
        getEntireCodes().forEach{
            if (it.cooldown == cooldown) redeemCodeList.add(it)
        }
        return redeemCodeList
    }


    fun getEntireCodes(): List<RedeemCode> {
        return redeemCodeDao.getEntireCodes()
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