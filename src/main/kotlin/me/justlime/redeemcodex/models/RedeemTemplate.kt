package me.justlime.redeemcodex.models

import org.bukkit.inventory.ItemStack

data class RedeemTemplate(
    val name: String, //TODO Implement Template Rename

    var defaultEnabledStatus: Boolean,
    var syncEnabledStatus: Boolean,

    var commands: MutableList<String>,
    var syncCommands: Boolean,

    var duration: String,
    var syncDuration: Boolean,

    var cooldown: String,
    var syncCooldown: Boolean,

    var pin: Int = 0,
    var syncPin: Boolean,

    var redemption: Int = 1,
    var syncRedemption: Boolean,

    var playerLimit: Int = 1,
    var syncPlayerLimit: Boolean,

    var defaultSync: Boolean,
    var syncLockedStatus: Boolean,

    var permissionRequired: Boolean,
    var permissionValue: String,
    var syncPermission: Boolean,

    var messages: MessageState,
    var syncMessages: Boolean,

    var sound: String,
    var soundVolume: Float,
    var soundPitch: Float,

    var syncSound: Boolean,

    var rewards: MutableList<ItemStack>,
    var syncRewards: Boolean,

    var target: MutableList<String> = mutableListOf(),
    var syncTarget: Boolean,
    )
