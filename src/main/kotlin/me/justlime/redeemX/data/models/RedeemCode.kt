package me.justlime.redeemX.data.models

import java.time.LocalDateTime

data class RedeemCode(
    val id: Int = 0,
    val code: String,
    val commands: List<String>,
    var maxRedeems: Int,
    var maxPerPlayer: Int,
    var isEnabled: Boolean,
    val expiry: LocalDateTime?,
    val permission: String?,
    val secureCode: String?,
    val specificPlayerId: String?,
)