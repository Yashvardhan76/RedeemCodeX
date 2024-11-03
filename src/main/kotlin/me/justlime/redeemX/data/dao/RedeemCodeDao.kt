package me.justlime.redeemX.data.dao

import me.justlime.redeemX.data.models.RedeemCode

interface RedeemCodeDao {
    fun createTable()
    fun insert(redeemCode: RedeemCode): Boolean
    fun findById(id: Int): RedeemCode?
    fun findByCode(code: String): RedeemCode?
    fun update(redeemCode: RedeemCode): Boolean
    fun deleteById(id: Int): Boolean
    fun getAllCodes(): List<RedeemCode>
    fun deleteAll(): Boolean
}