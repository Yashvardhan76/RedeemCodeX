package me.justlime.redeemX.data.local

import me.justlime.redeemX.enums.JProperty
import me.justlime.redeemX.models.RedeemCode

interface RedeemCodeDao {
    fun createTable()
    fun upsertCode(redeemCode: RedeemCode): Boolean
    fun upsertCodes(redeemCodes: List<RedeemCode>): Boolean
    fun deleteEntireCodes(): Boolean
    fun deleteByCode(code: String): Boolean
    fun get(code: String): RedeemCode?
    fun getCachedCodes(): List<String>
    fun getCachedTargetList(): MutableMap<String, MutableList<String>>
    fun getByProperty(property: JProperty, value: String): List<RedeemCode>
    fun getEntireCodes(): List<RedeemCode>
    fun getTemplateCodes(template: String): List<RedeemCode>

}