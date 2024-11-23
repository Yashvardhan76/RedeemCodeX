package me.justlime.redeemX.data.dao

import me.justlime.redeemX.data.models.RedeemCode

interface RedeemCodeDao {
    fun createTable()

    fun upsert(redeemCode: RedeemCode): Boolean

    fun deleteAll(): Boolean
    fun deleteByCode(code: String): Boolean

    fun get(code: String): RedeemCode?
    fun getTemplate(template: String): RedeemCode?
    fun getAllCodes(): List<RedeemCode>
    fun getTemplateCodes(template: String): List<RedeemCode>

}