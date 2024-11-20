package me.justlime.redeemX.data.models

import java.time.LocalDateTime

data class RedeemCode(
    val code: String,
    var commands: MutableMap<Int, String> ,
    var storedTime: LocalDateTime?,
    var duration: String?,
    var isEnabled: Boolean,
    var maxRedeems: Int,
    var maxPlayers: Int,
    var permission: String?,
    var pin: Int,
    var target: MutableList<String?>,
    var usage: MutableMap<String, Int>,
    var template: String,
    var storedCooldown: LocalDateTime?,
    var cooldown: String?
)