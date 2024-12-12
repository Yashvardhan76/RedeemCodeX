package me.justlime.redeemX.utilities

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
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

    private fun calculateExpiry(storedTime: LocalDateTime, duration: String): LocalDateTime? {
        val amount = duration.dropLast(1).toIntOrNull() ?: return null
        val unit = duration.takeLast(1)
        return when (unit) {
            "s" -> storedTime.plusSeconds(amount.toLong())
            "m" -> storedTime.plusMinutes(amount.toLong())
            "h" -> storedTime.plusHours(amount.toLong())
            "d" -> storedTime.plusDays(amount.toLong())
            "mo" -> storedTime.plusMonths(amount.toLong())
            "y" -> storedTime.plusYears(amount.toLong())
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

    fun convertDurationToSeconds(duration: String): String {
        val timeUnitToSeconds = mapOf("s" to 1L, "m" to 60L, "h" to 3600L, "d" to 86400L, "mo" to 2592000L, "y" to 31536000L)
        val amount = duration.dropLast(1).toLongOrNull() ?: return "0s"
        val unit = duration.takeLast(1)
        return (amount * (timeUnitToSeconds[unit] ?: 1L)).toString() + "s"
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
            "max_redeems" to placeholder.maxRedeemsPerPlayer,
            "max_players" to placeholder.maxPlayersCanRedeem,
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