package me.justlime.redeemX.data.dao

import me.justlime.redeemX.data.models.RedeemCode

interface RedeemCodeDao {
    fun createTable()
    fun insert(redeemCode: RedeemCode): Boolean
    fun update(redeemCode: RedeemCode): Boolean

    fun deleteAll(): Boolean
    fun deleteByCode(code: String): Boolean
    fun getByCode(code: String): RedeemCode?

    fun addCommand(code: String, command: String): Boolean
    fun setCommand(code: String, command: String): Boolean
    fun setCommandById(code: String, id: Int, command: String): Boolean
    fun getAllCommands(code: String): Map<Int, String>?
    fun getCommandById(code: String,id: Int): String?
    fun deleteCommandById(code: String, id: Int): Boolean
    fun deleteAllCommands(code: String): Boolean

    fun getAllCodes(): List<RedeemCode>
    fun isExpired(code: String): Boolean
}