package me.justlime.redeemX.data.repository

import me.justlime.redeemX.data.local.RedeemCodeDao
import me.justlime.redeemX.data.models.RedeemCode

class RedeemCodeRepository(private val redeemCodeDao: RedeemCodeDao): RedeemCodeRepositoryHandler {

    override fun getCode(code: String): RedeemCode {
        return redeemCodeDao.get(code) ?: throw Exception("Code not found")
    }

    override fun getEntireCodes(): List<RedeemCode> {
        return redeemCodeDao.getEntireCodes()
    }

    /** Provide the Template specs **/
    override fun getTemplate(template: String): RedeemCode {
        //Bad Code what if db empty? TODO

        return redeemCodeDao.getTemplate(template) ?: throw Exception("Template not found")
    }

    override fun getEntireTemplates(): List<String> {
        return redeemCodeDao.getEntireCodes().map { it.template }.distinct()
    }

    override fun getTemplateCodes(template: String): List<RedeemCode> {
        return redeemCodeDao.getTemplateCodes(template)
    }

    override fun upsertCode(redeemCode: RedeemCode): Boolean {
        return redeemCodeDao.upsert(redeemCode)
    }


    override fun deleteCode(code: String): Boolean {
        return redeemCodeDao.deleteByCode(code)
    }

    override fun deleteEntireCodes(): Boolean {
        return redeemCodeDao.deleteEntireCodes()
    }

}