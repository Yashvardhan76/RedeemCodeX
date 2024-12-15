package me.justlime.redeemX.models

import java.sql.Timestamp

data class RedeemCodeDatabase(
    val code: String,
    val commands: String,
    val validFrom: Timestamp,
    val duration: String,
    val isEnabled: Boolean,
    val redemption: Int,
    val limit: Int,
    val permission: String,
    val pin: Int,
    val target: String,
    val usedBy: String,
    val template: String,
    val templateLocked: Boolean,
    val lastRedeemed: Timestamp,
    val cooldown: String
)