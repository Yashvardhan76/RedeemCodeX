/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 *
 */


package me.justlime.redeemcodex.data.repository

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.data.local.RedeemCodeDao
import me.justlime.redeemcodex.models.RedeemCode
import me.justlime.redeemcodex.models.RedeemTemplate
import me.justlime.redeemcodex.models.SoundState
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.Sound

/**
 * This repository class is responsible for managing redeem codes from database.
 */
class RedeemCodeRepository(plugin: RedeemCodeX) {
    private val redeemCodeDao: RedeemCodeDao = plugin.redeemCodeDB

    fun getCode(code: String): RedeemCode? {
        return redeemCodeDao.get(code)
    }

    fun fetch() {
        redeemCodeDao.fetch()
    }

    /**Used this for getting list of cached Code useful for listing**/
    fun getCachedCode(): List<String> {
        return redeemCodeDao.getCachedCodes()
    }

    /**
     * List<code<Targets-Name>>
     */

    fun getCachedTargetList(): MutableMap<String, MutableList<String>> {
        return redeemCodeDao.getCachedTargets()
    }

    fun getCachedUsageList(): MutableMap<String, MutableMap<String, Int>> {
        return redeemCodeDao.getCachedUsages()
    }

    fun getCodesByTemplate(template: String, lockedStatus: Boolean): List<RedeemCode> {
        return redeemCodeDao.getTemplateCodes(template, lockedStatus)
    }

    /**
     * Get Data From template.yml. If sync is false no data will be sync
     *
     * @param redeemCode The redeem code to modify.
     * @param template The new template for the redeem code.
     * @return `true` if the operation was successful, `false` otherwise.
     *
     **/
    fun templateToRedeemCode(redeemCode: RedeemCode, template: RedeemTemplate): Boolean {

        if (template.name.isBlank()) return false
        redeemCode.apply {
            this.template = template.name
            if (template.defaultSync) {
                if (template.syncEnabledStatus) enabledStatus = template.defaultEnabledStatus
                if (template.syncPermission) permission = template.permissionValue
                if (template.syncDuration) duration = template.duration
                if (template.syncCooldown) cooldown = template.cooldown
                if (template.syncPin) pin = template.pin
                if (template.syncRedemption) redemption = template.redemption
                if (template.syncPlayerLimit) playerLimit = template.playerLimit
                if (template.syncMessages) messages = template.messages
                if (template.syncSound) sound = SoundState(
                    sound = try {
                        Sound.valueOf(template.sound.uppercase())
                    } catch (e: Exception) {
                        null
                    }, volume = template.soundVolume, pitch = template.soundPitch
                )
                if (template.syncRewards) rewards = template.rewards
                if (template.syncTarget) target = template.target
                if (template.syncCommands) commands = template.commands
                if (template.syncLockedStatus) this.sync = template.defaultSync
                modified = JService.getCurrentTime()
            } else return false
        }
        return true
    }

    fun clearUsage(redeemCode: RedeemCode, player: String = ""): Boolean {
        if (player.isBlank()) {
            redeemCode.usedBy.clear()
            return true
        }
        if (redeemCode.usedBy.containsKey(player)) {
            redeemCode.usedBy.remove(player)
            return true
        }
        return false
    }

    fun clearRedeemedTime(redeemCode: RedeemCode) {
        redeemCode.lastRedeemed.clear()
    }

    fun upsertCode(redeemCode: RedeemCode): Boolean {
        return redeemCodeDao.upsertCode(redeemCode)
    }

    fun upsertCodes(redeemCodes: List<RedeemCode>): Boolean {
        return redeemCodeDao.upsertCodes(redeemCodes)
    }

    fun deleteCode(code: String): Boolean {
        return redeemCodeDao.deleteByCode(code)
    }

    fun deleteCodes(codes: List<String>): Boolean {
        return redeemCodeDao.deleteByCodes(codes)
    }

    fun deleteAllCodes(): Boolean {
        return redeemCodeDao.deleteAllCodes()
    }
}