package me.justlime.redeemX.data.models

import java.time.LocalDateTime

//private val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
//private val timeZone: ZonedDateTime = ZonedDateTime.now(timeZoneId)
//private val currenTime: LocalDateTime = timeZone.toLocalDateTime()

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
    var target: String?,
    var usage: MutableMap<String, Int>,
)