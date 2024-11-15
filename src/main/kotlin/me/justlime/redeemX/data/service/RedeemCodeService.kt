package me.justlime.redeemX.data.service

import me.justlime.redeemX.RedeemX
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class RedeemCodeService(val plugin: RedeemX) {
    private val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")

    fun isExpired(code: String): Boolean {
        val redeemCode = plugin.redeemCodeDB.get(code) ?: return false
        val storedTime = redeemCode.storedTime ?: return false
        val duration = redeemCode.duration ?: return false

        // Dynamically fetch current time for each call
        val currentTime = ZonedDateTime.now(timeZoneId).toLocalDateTime()
        val expiryTime = calculateExpiry(storedTime, duration)

        return expiryTime?.isBefore(currentTime) ?: false
    }

    private fun calculateExpiry(time: LocalDateTime, duration: String): LocalDateTime? {
        val amount = duration.dropLast(1).toIntOrNull() ?: return null
        val unit = duration.takeLast(1)
        return when (unit) {
            "s" -> time.plusSeconds(amount.toLong())
            "m" -> time.plusMinutes(amount.toLong())
            "h" -> time.plusHours(amount.toLong())
            "d" -> time.plusDays(amount.toLong())
            "mo" -> time.plusMonths(amount.toLong())
            "y" -> time.plusYears(amount.toLong())
            else -> null
        }
    }

//    fun getAllCommands(code: String): MutableMap<Int, String>? {
//        val commands = get(code)?.commands ?: return null
//        if (commands.values.toString().trim().isEmpty()) return null
//        return commands
//    }
//
//    fun getCommandById(code: String, id: Int): String? {
//        val commands = get(code)?.commands?.containsKey(id)?.toString() ?: return null
//        if (commands.trim().isEmpty()) return null
//        return commands
//    }

}