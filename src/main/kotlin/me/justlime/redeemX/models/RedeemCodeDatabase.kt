package me.justlime.redeemX.models

import java.sql.Timestamp

data class RedeemCodeDatabase(
    val code: String,
    val commands: String,
    val storedTime: Timestamp,
    val duration: String,
    val isEnabled: Boolean,
    val redemptionLimit: Int,
    val playerLimit: Int,
    val permission: String,
    val pin: Int,
    val target: String,
    val usedBy: String,
    val template: String,
    val templateLocked: Boolean,
    val storedCooldown: Timestamp,
    val cooldown: String
)