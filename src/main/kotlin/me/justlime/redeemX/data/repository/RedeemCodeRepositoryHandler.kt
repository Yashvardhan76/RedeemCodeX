package me.justlime.redeemX.data.repository

import me.justlime.redeemX.data.models.RedeemCode

interface RedeemCodeRepositoryHandler{
    fun getCode(code: String): RedeemCode
    fun getEntireCodes(): List<RedeemCode>
    fun getTemplate(template: String): RedeemCode
    fun getEntireTemplates(): List<String>
    fun getTemplateCodes(template: String): List<Any>
    fun upsertCode(redeemCode: RedeemCode): Boolean
    fun deleteCode(code: String): Boolean
    fun deleteEntireCodes(): Boolean
}
