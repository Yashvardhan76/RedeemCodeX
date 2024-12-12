package me.justlime.redeemX.data.local

import me.justlime.redeemX.models.RedeemCode

interface RedeemCodeDao {
    fun createTable()
    fun upsert(redeemCode: RedeemCode): Boolean
    fun deleteEntireCodes(): Boolean
    fun deleteByCode(code: String): Boolean
    fun get(code: String): RedeemCode?
    fun getCachedCodes(): List<String>
    fun getTemplate(template: String): RedeemCode?
    fun getEntireCodes(): List<RedeemCode>
    fun getTemplateCodes(template: String): List<RedeemCode>

}