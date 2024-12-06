package me.justlime.redeemX.data.repository

import me.justlime.redeemX.data.local.RedeemCodeDao
import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.data.models.RedeemTemplate

class RedeemCodeRepository(private val redeemCodeDao: RedeemCodeDao){

    fun getCode(code: String): RedeemCode {
        return redeemCodeDao.get(code) ?: throw Exception("Code not found")
    }

    fun getEntireCodes(): List<RedeemCode> {
        return redeemCodeDao.getEntireCodes()
    }

    /** Provide the Template specs **/
    fun getTemplateCodes(template: String): List<RedeemCode> {
        return redeemCodeDao.getTemplateCodes(template)
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