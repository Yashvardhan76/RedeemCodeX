package me.justlime.redeemX.utilities

import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
import org.bukkit.ChatColor
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.regex.Pattern

class RedeemCodeService {
    private val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
    val currentTime: LocalDateTime = ZonedDateTime.now(timeZoneId).toLocalDateTime()

    fun adjustDuration(existingDuration: String, adjustmentDuration: String, isAdding: Boolean): String {
        val timeUnitToSeconds = mapOf(
            "y" to 31536000L,
            "mo" to 2592000L,
            "d" to 86400L,
            "h" to 3600L,
            "m" to 60L,
            "s" to 1L
        )
        val secondsToTimeUnit = timeUnitToSeconds.entries.sortedByDescending { it.value }

        val existingAmount = existingDuration.dropLast(1).toLongOrNull() ?: return "0s"
        val existingUnit = existingDuration.takeLast(1)
        val adjustmentAmount = adjustmentDuration.dropLast(1).toLongOrNull() ?: return "0s"
        val adjustmentUnit = adjustmentDuration.takeLast(1)

        val existingSeconds = existingAmount * (timeUnitToSeconds[existingUnit] ?: return "0s")
        val adjustmentSeconds = adjustmentAmount * (timeUnitToSeconds[adjustmentUnit] ?: return "0s")

        val adjustedSeconds = if (isAdding) existingSeconds + adjustmentSeconds else existingSeconds - adjustmentSeconds
        if (adjustedSeconds <= 0) return "0s"

        // Convert seconds into a human-readable duration with multiple units
        val result = StringBuilder()
        var remainingSeconds = adjustedSeconds

        for ((unit, secondsInUnit) in secondsToTimeUnit) {
            if (remainingSeconds >= secondsInUnit) {
                val amount = remainingSeconds / secondsInUnit
                remainingSeconds %= secondsInUnit
                result.append("${amount}$unit")
            }
        }
        return result.toString()
    }

    fun isDurationValid(duration: String): Boolean {
        if (duration.isBlank()) return false
        if (duration.length < 2) return false

        // Valid units: seconds (s), minutes (m), hours (h), days (d), weeks (w), months (mo), years (y)
        val pattern = Regex("""^(\d+)([smhdw]|mo|y)$""")
        return pattern.matches(duration) && duration.takeWhile { it.isDigit() }.toInt() > 0
    }

    fun convertDurationToSeconds(duration: String): String {
        val timeUnitToSeconds = mapOf("s" to 1L, "m" to 60L, "h" to 3600L, "d" to 86400L, "mo" to 2592000L, "y" to 31536000L)
        val pattern = Regex("""^(\d+)(s|m|h|d|mo|y)$""")
        val match = pattern.matchEntire(duration) ?: return "0s"

        val (amount, unit) = match.destructured
        return (amount.toLong() * (timeUnitToSeconds[unit] ?: 1L)).toString() + "s"
    }

    fun isExpired(redeemCode: RedeemCode): Boolean {
        val storedTime = redeemCode.storedTime.toLocalDateTime()
        val duration = redeemCode.duration ?: return false
        val expiryTime = storedTime.plusSeconds(convertDurationToSeconds(duration).dropLast(1).toLongOrNull() ?: return false)
        return expiryTime.isBefore(currentTime)
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

    fun removeColors(message: String): String {
        // Regex to match Minecraft color codes (§x§r§g§b§x§x§r) and simpler §x formats
        val colorCodePattern = Regex("\u00A7[x0-9a-fA-F](\u00A7[0-9a-fA-F]){5}|\u00A7[0-9a-fA-F]")

        // Remove any color codes
        var plainMessage = message.replace(colorCodePattern, "")

        // Remove alternate color codes like &#FFFFFF or &x
        plainMessage = plainMessage.replace(Regex("&[0-9a-fA-F]|&#[a-fA-F0-9]{6}"), "")

        return plainMessage
    }

    fun applyPlaceholders(message: String, placeholder: CodePlaceHolder): String {
        val placeholders: Map<String,String> = mapOf(
            "code" to placeholder.code,
            "sender" to placeholder.sender.name,
            "args" to placeholder.args.joinToString(" "),
            "commands" to "\n${placeholder.command}",
            "id" to placeholder.commandId,
            "duration" to placeholder.duration,
            "enabled" to placeholder.isEnabled,
            "max_redeems" to placeholder.maxRedeems,
            "max_players" to placeholder.maxPlayers,
            "permission" to placeholder.permission,
            "pin" to placeholder.pin,
            "target" to placeholder.target,
            "usage" to placeholder.usage,
            "template" to placeholder.template,
            "templateLocked" to placeholder.templateLocked,
            "cooldown" to placeholder.cooldown,
            "expired" to placeholder.isExpired,
            "minLength" to placeholder.minLength,
            "maxLength" to placeholder.maxLength,
            "code_generate_digit" to placeholder.codeGenerateDigit
        )
        return placeholders.entries.fold(message) { msg, (placeholder, value) ->
            msg.replace("{$placeholder}", value)
        }
    }
}