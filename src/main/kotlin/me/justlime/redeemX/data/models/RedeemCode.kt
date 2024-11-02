package me.justlime.redeemX.data.models

import java.time.LocalDateTime

data class RedeemCode(
    val id: Int = 0,
    val code: String,
    val commands: List<String>,
    val maxRedeems: Int,
    val maxPerPlayer: Int,
    val isEnabled: Boolean,
    val expiry: LocalDateTime?,
    val permission: String?,
    val secureCode: String?,
    val specificPlayerId: String?,
)