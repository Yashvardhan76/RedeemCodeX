package me.justlime.redeemX.models

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

    var message: List<String>,
    var syncMessages: Boolean,

    var sound: String,
    var syncSound: Boolean,

    var rewards: MutableList<ItemStack>,
    var syncRewards: Boolean,

    var target: MutableList<String> = mutableListOf(),
    var syncTarget: Boolean,
    ) {
    fun toRedeemCode(code: String): RedeemCode {
        return RedeemCode(
            code = code,
            enabledStatus = this.defaultEnabledStatus,
            template = this.name,
            sync = this.defaultSync,
            duration = this.duration,
            cooldown = this.cooldown,
            permission = this.permissionValue,
            pin = this.pin,
            redemption = this.redemption,
            playerLimit = this.playerLimit,
            usedBy = mutableMapOf(),
            validFrom = java.sql.Timestamp(System.currentTimeMillis()),
            lastRedeemed = mutableMapOf(),
            target = this.target,
            commands = this.commands,
            messages = this.message.joinToString("\n"),
            sound = this.sound,
            rewards = this.rewards,
            modified = java.sql.Timestamp(System.currentTimeMillis())
        )
    }
}
