package me.justlime.redeemX.data.dao

import me.justlime.redeemX.data.models.RedeemCode

interface RedeemCodeDao {
    fun createTable()
    fun insert(redeemCode: RedeemCode): Boolean
    fun update(redeemCode: RedeemCode): Boolean

    fun deleteAll(): Boolean
    fun deleteByCode(code: String): Boolean
    fun get(code: String): RedeemCode?

    fun getAllCommands(code: String): MutableMap<Int, String>?
    fun getCommandById(code: String, id: Int): String?

    fun getAllCodes(): List<RedeemCode>
    fun isExpired(code: String): Boolean
}