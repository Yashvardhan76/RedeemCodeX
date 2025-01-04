package me.justlime.redeemcodex.models

import org.bukkit.inventory.ItemStack
import java.sql.Timestamp

data class RedeemCode(
    val code: String,
    var enabledStatus: Boolean,

    var template: String, //Blank for disabled
    var sync: Boolean,

    var duration: String, //0s for disabled
    var cooldown: String, //0s for disabled

    var permission: String, //Blank for disabled
    var pin: Int, //0 for disabled

    var redemption: Int, //0 for infinite Redemption limit
    var playerLimit: Int, //0 for infinite player limit

    var usedBy: MutableMap<String, Int>,

    var validFrom: Timestamp,
    var lastRedeemed: MutableMap<String, Timestamp>,

    var target: MutableList<String>, //Blank for disabled
    var commands: MutableList<String>, //Empty list for disabled

    var rewards: MutableList<ItemStack> = mutableListOf(),
    var messages: String = "",
    var sound: String = "",

    var modified: Timestamp
){
    fun toRedeemTemplate(template: String): RedeemTemplate {
        return RedeemTemplate(
            name = template,
            defaultEnabledStatus = this.enabledStatus,
            commands = this.commands,
            duration = this.duration,
            redemption = this.redemption,
            playerLimit = this.playerLimit,
            permissionRequired = this.permission.isNotBlank(),
            permissionValue = this.permission,
            pin = this.pin,
            defaultSync = this.sync,
            cooldown = this.cooldown,
            message = mutableListOf(this.messages),
            sound = this.sound,
            rewards = this.rewards,
            target = mutableListOf(),
            syncEnabledStatus = false,
            syncLockedStatus = false,
            syncCommands = true,
            syncDuration = true,
            syncCooldown = true,
            syncPin = true,
            syncRedemption = true,
            syncPlayerLimit = true,
            syncPermission = true,
            syncMessages = true,
            syncSound = true,
            syncRewards = true,
            syncTarget = true,
        )
    }
}