package me.justlime.redeemX.data.models

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
private val timeZone: ZonedDateTime = ZonedDateTime.now(timeZoneId)
private val currenTime: LocalDateTime = timeZone.toLocalDateTime()

data class RedeemCode(
    val code: String,
    var commands: MutableMap<Int, String> = mutableMapOf(),
    var storedTime: LocalDateTime? = null,
    var duration: String? = null,
    var isEnabled: Boolean = true,
    var maxRedeems: Int = 1,
    var maxPlayers: Int = 1,
    var permission: String?,
    var pin: Int = -1,
    var target: String? = null,
    var usage: MutableMap<String, Int> = mutableMapOf(),
)