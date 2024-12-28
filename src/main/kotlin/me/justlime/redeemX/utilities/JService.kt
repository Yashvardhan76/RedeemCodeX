package me.justlime.redeemX.utilities

import me.clip.placeholderapi.PlaceholderAPI
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.sql.Timestamp
import java.time.Instant
import java.util.regex.Pattern

object JService {

    fun getCurrentTime(): Timestamp {
        return Timestamp.from(Instant.now())
    }

    fun adjustDuration(existingDuration: String, adjustmentDuration: String, isAdding: Boolean): String {
        val totalExistingSeconds = parseDurationToSeconds(existingDuration)
        val totalAdjustmentSeconds = parseDurationToSeconds(adjustmentDuration)

        val adjustedSeconds = if (isAdding) totalExistingSeconds + totalAdjustmentSeconds else totalExistingSeconds - totalAdjustmentSeconds
        if (adjustedSeconds <= 0) return "0s"

        return formatSecondsToDuration(adjustedSeconds)
    }

    fun formatSecondsToDuration(seconds: Long): String {
        val timeUnitToSeconds = mapOf(
            "y" to 31536000L, "mo" to 2592000L, "d" to 86400L, "h" to 3600L, "m" to 60L, "s" to 1L
        )
        val sortedUnits = timeUnitToSeconds.entries.sortedByDescending { it.value }
        val result = StringBuilder()
        var remainingSeconds = seconds

        for ((unit, secondsInUnit) in sortedUnits) {
            if (remainingSeconds >= secondsInUnit) {
                val amount = remainingSeconds / secondsInUnit
                remainingSeconds %= secondsInUnit
                result.append("${amount}${unit}")
            }
        }
        return result.toString()
    }

    fun parseDurationToSeconds(duration: String): Long {
        val regex = """(\d+)(y|mo|d|h|m|s)""".toRegex()
        val timeUnitToSeconds = mapOf(
            "y" to 31536000L, "mo" to 2592000L, "d" to 86400L, "h" to 3600L, "m" to 60L, "s" to 1L
        )

        return regex.findAll(duration).sumOf { match ->
            val value = match.groupValues[1].toLongOrNull() ?: 0L
            val unit = match.groupValues[2]
            value * (timeUnitToSeconds[unit] ?: 0L)
        }
    }

    fun isDurationValid(duration: String): Boolean {
        if (duration.isBlank()) return false
        if (duration.length < 2) return false

        // Define valid units dynamically
        val validUnits = listOf("y", "mo", "d", "h", "m", "s").joinToString("|")
        val pattern = Regex("""^(\d+($validUnits))+${'$'}""")

        // Match against the pattern and ensure numbers are valid
        return pattern.matches(duration) && Regex("""\d+""").findAll(duration).all { it.value.toInt() > 0 }
    }

    fun isExpired(redeemCode: RedeemCode): Boolean {
        val time = redeemCode.validFrom
        val duration = redeemCode.duration

        val expiryTimeMillis = time.time + parseDurationToSeconds(duration) * 1000
        return getCurrentTime().time > expiryTimeMillis  //29oct > 30oct (false)
    }

    fun onCoolDown(cooldown: String, lastRedeemed: MutableMap<String, Timestamp>, player: String): Boolean {
        val cooldownTimeMillis = parseDurationToSeconds(cooldown) * 1000
        val lastRedeemedTimeMillis = lastRedeemed[player]?.time ?: 0L
        val cooldownTime = lastRedeemedTimeMillis + cooldownTimeMillis
        return getCurrentTime().time < cooldownTime //29oct < 28oct + 2d = 30oct (true)

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

    fun applyPlaceholders(message: String, placeholder: CodePlaceHolder, isPlaceholderHooked: ()->Boolean = { false }): String {
        val placeholders: Map<String, String> = mapOf(
            "code" to placeholder.code,
            "sender" to placeholder.sender.name,
            "args" to placeholder.args.joinToString(" "),
            "commands" to "\n${placeholder.command}",
            "id" to placeholder.commandId,
            "duration" to placeholder.duration,
            "enabled" to placeholder.isEnabled,
            "max_redemption" to placeholder.redemptionLimit,
            "max_player_limit" to placeholder.playerLimit,
            "permission" to placeholder.permission,
            "pin" to placeholder.pin,
            "target" to placeholder.target,
            "usedBy" to placeholder.usedBy,
            "redeemed_by" to placeholder.redeemedBy,
            "total_player_redeemed" to placeholder.totalPlayerUsage,
            "total_redemption" to placeholder.totalRedemption,
            "expiry" to placeholder.validTo,

            "template" to placeholder.template,
            "locked" to placeholder.templateLocked,
            "cooldown" to placeholder.cooldown,
            "expired" to placeholder.isExpired,
            "minLength" to placeholder.minLength,
            "maxLength" to placeholder.maxLength,
            "code_generate_digit" to placeholder.codeGenerateDigit,
            "property" to placeholder.property,
            "player" to placeholder.player
        )
        val text = placeholders.entries.fold(message) { msg, (placeholder, value) ->
            msg.replace("{$placeholder}", value)
        }

        return if (placeholder.sender is Player && isPlaceholderHooked()) PlaceholderAPI.setPlaceholders(placeholder.sender as Player, text) else text
    }
}