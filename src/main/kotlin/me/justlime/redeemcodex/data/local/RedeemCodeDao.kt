/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *  This file handles the core logic for redeeming codes and managing associated data.
 *
 */


package me.justlime.redeemcodex.data.local

import me.justlime.redeemcodex.enums.JProperty
import me.justlime.redeemcodex.models.RedeemCode

interface RedeemCodeDao {
    fun createTable()
    fun upsertCode(redeemCode: RedeemCode): Boolean
    fun upsertCodes(redeemCodes: List<RedeemCode>): Boolean
    fun deleteByCode(code: String): Boolean
    fun deleteByCodes(codes: List<String>): Boolean
    fun deleteAllCodes(): Boolean
    fun get(code: String): RedeemCode?
    fun fetch()
    fun getCachedCodes(): List<String>
    fun getCachedTargets(): MutableMap<String, MutableList<String>>
    fun getCachedUsages(): MutableMap<String, MutableMap<String,Int>>
    fun getByProperty(property: JProperty, value: String): List<RedeemCode>
    fun getAllCodes(): List<RedeemCode>
    fun getTemplateCodes(template: String, lockedStatus: Boolean): List<RedeemCode>

}