package me.justlime.redeemX.utilities

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.ConfigManager
import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.state.RedeemCodeState
import org.bukkit.ChatColor
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.regex.Pattern

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

    fun mapResultSetToRedeemCode(result: ResultSet): RedeemCode {
        val commandsString = result.getString("commands")
        val usageString = result.getString("usedBy")
        val storedTime = result.getTimestamp("storedTime")?.toLocalDateTime()
        val commandMap = parseToMapId(commandsString)
        val playerUsageMap = parseToMapString(usageString)

        val targetList: MutableList<String?> = result.getString("target").split(",").toMutableList()

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
            target = targetList,
            usage = playerUsageMap,
            template = result.getString("template"),
            storedCooldown = result.getTimestamp("storedCooldown")?.toLocalDateTime(),
            cooldown = result.getString("cooldown"),
            templateLocked = result.getBoolean("templateLocked")
        )
    }
    fun parseToId(string: String?): String {
        if (string.isNullOrBlank()) return ""
        return string.trim()
            .split(",")
            .mapIndexed { index, entry -> "$index: ${entry.trim()}" }
            .joinToString(", ")
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

    private fun parseToMapString(input: String?, separator: String = ":"): MutableMap<String, Int> {
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

    fun handleDurationModification(action: String, adjustmentDuration: String?, state: RedeemCodeState,config: ConfigManager) {
        val service = RedeemCodeService(plugin)
        val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
        val currentTime: LocalDateTime = LocalDateTime.now(timeZoneId)
        if (state.storedTime == null) state.storedTime = currentTime

        val existingDuration = state.duration ?: "0s"
        val durationValue = adjustmentDuration ?: "0"

        when (action.lowercase()) {
            "set" -> {
                state.storedTime = currentTime
                state.duration = service.adjustDuration("0s", durationValue, isAdding = true).toString() + 's'
            }

            "add" -> state.duration =
                service.adjustDuration(existingDuration, durationValue, isAdding = true).toString() + 's'

            "remove" -> {
                val duration =
                    service.adjustDuration(existingDuration, durationValue, isAdding = false).toString() + 's'
                state.duration = if ((duration.dropLast(1).toIntOrNull() ?: -1) < 0) null else duration
            }

            else -> {
                config.sendMsg(
                    "commands.modify.duration-invalid", state = state
                )
            }
        }
    }

    fun applyColors(message: String): String {
        var coloredMessage = ChatColor.translateAlternateColorCodes('&', message)
        val hexPattern = Pattern.compile("&#[a-fA-F0-9]{6}")
        val matcher = hexPattern.matcher(coloredMessage)
        while (matcher.find()) {
            val hexCode = matcher.group()
            val bukkitHexCode = "\u00A7x" + hexCode.substring(2).toCharArray().joinToString("") { "\u00A7$it" }
            coloredMessage = coloredMessage.replace(hexCode, bukkitHexCode)
        }
        return coloredMessage
    }
}