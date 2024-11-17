package me.justlime.redeemX.data.service

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.models.RedeemCode
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class RedeemCodeService(val plugin: RedeemX) {
    private val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
    val currentTime: LocalDateTime = ZonedDateTime.now(timeZoneId).toLocalDateTime()

    fun isExpired(code: String): Boolean {
        val redeemCode = plugin.redeemCodeDB.get(code) ?: return false
        val storedTime = redeemCode.storedTime ?: return false
        val duration = redeemCode.duration ?: return false

        // Dynamically fetch current time for each call

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
    fun mapResultSetToRedeemCode(result: ResultSet): RedeemCode {
        val commandsString = result.getString("commands")
        val usageString = result.getString("usedBy")
        val storedTime = result.getTimestamp("storedTime")?.toLocalDateTime()
        val commandMap = parseToMapId(commandsString)
        val playerUsageMap = parseToMapString(usageString)

        return RedeemCode(
            code = result.getString("code"),
            commands = commandMap,
            storedTime = storedTime,
            duration = result.getString("duration"),
            isEnabled = result.getBoolean("isEnabled"),
            maxRedeems = result.getInt("max_redeems"),
            maxPlayers = result.getInt("max_player"),
            permission = result.getString("permission"),
            pin = result.getInt("pin"),
            target = result.getString("target"),
            usage = playerUsageMap,
        )
    }

    fun parseToMapId(string: String?, separator: String = ":"): MutableMap<Int, String> {
        if (string.isNullOrBlank()) return mutableMapOf()
        val input = string.trim()
        val resultMap = mutableMapOf<Int, String>()

        for (entry in input.split(",")) {
            val parts = entry.split(separator)
            if (parts.size == 2) {
                val key = parts[0].trim().toIntOrNull()
                val value = parts[1].takeIf { it.isNotBlank() }
                if (key != null && value != null) {
                    resultMap[key] = value
                }
            }
        }

        return resultMap
    }

    fun parseToMapString(input: String?, separator: String = ":"): MutableMap<String, Int> {
        if (input.isNullOrBlank()) return mutableMapOf()
        val resultMap = mutableMapOf<String, Int>()

        for (entry in input.split(",")) {
            val parts = entry.split(separator)
            if (parts.size == 2) {
                val key = parts[0].takeIf { it.isNotBlank() }
                val value = parts[1].toIntOrNull()
                if (key != null && value != null) {
                    resultMap[key] = value
                }
            }
        }

        return resultMap
    }

    fun adjustDuration(existingDuration: String, adjustmentDuration: String, isAdding: Boolean): Long {
        val timeUnitToSeconds =
            mapOf("s" to 1L, "m" to 60L, "h" to 3600L, "d" to 86400L, "mo" to 2592000L, "y" to 31536000L)

        val existingAmount = existingDuration.dropLast(1).toLongOrNull() ?: return 0L
        val existingUnit = existingDuration.takeLast(1)
        val adjustmentAmount = adjustmentDuration.dropLast(1).toLongOrNull() ?: return 0L
        val adjustmentUnit = adjustmentDuration.takeLast(1)

        val existingSeconds = existingAmount * (timeUnitToSeconds[existingUnit] ?: return 0L)
        val adjustmentSeconds = adjustmentAmount * (timeUnitToSeconds[adjustmentUnit] ?: return 0L)

        return if (isAdding) existingSeconds + adjustmentSeconds else existingSeconds - adjustmentSeconds
    }
}