/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 *
 */

package me.justlime.redeemcodex.utilities

import jdk.internal.joptsimple.internal.Messages.message
import me.clip.placeholderapi.PlaceholderAPI
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.models.RedeemCode
import net.kyori.adventure.text.minimessage.MiniMessage
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.sql.Timestamp
import java.time.Instant
import java.util.regex.Pattern

object JService {

    fun getCurrentTime(): Timestamp {
        return Timestamp.from(Instant.now())
    }

    var miniMessage = MiniMessage.miniMessage();

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
        return pattern.matches(duration) && Regex("""\d+""").findAll(duration).all { it.value.toInt() >= 0 }
    }

    fun isExpired(redeemCode: RedeemCode): Boolean {
        val time = redeemCode.validFrom
        val duration = redeemCode.duration

        val expiryTimeMillis = time.time + parseDurationToSeconds(duration) * 1000
        return getCurrentTime().time > expiryTimeMillis  //29oct > 30oct (false)
    }

    fun onCoolDown(cooldown: String, lastRedeemed: MutableMap<String, Timestamp>, player: String): Boolean {
        if (!isDurationValid(cooldown)) return false
        if (cooldown == "0s") return false
        val cooldownTimeMillis = parseDurationToSeconds(cooldown) * 1000
        val lastRedeemedTimeMillis = lastRedeemed[player]?.time ?: 0L
        return getCurrentTime().time < lastRedeemedTimeMillis + cooldownTimeMillis
        //Example: 29oct < 28oct + 2d = 30oct (true) [29oct - current date]
    }

    fun applyHexColors(message: String): String {
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
        // Regex to match Minecraft color codes (§x§r§g§b§x§x§r) and simpler §x formats, as well as formatting codes like §l, §n, etc.
        val colorAndFormatCodePattern = Regex("\u00A7[x0-9a-fA-F](\u00A7[0-9a-fA-F]){5}|\u00A7[0-9a-fk-orA-FK-OR]")

        // Remove any color or formatting codes
        var plainMessage = message.replace(colorAndFormatCodePattern, "")

        // Remove alternate color codes like &#FFFFFF or &x
        plainMessage = plainMessage.replace(Regex("&[0-9a-fA-F]|&#[a-fA-F0-9]{6}"), "")

        return plainMessage
    }

    fun createClickableMessage(message: String): TextComponent {
        // Regex to find text enclosed in single quotes.
        val pattern = Regex("`([^`]+)`")
        val baseComponent = TextComponent()
        var lastIndex = 0

        // Process all matches of text enclosed in single quotes.
        pattern.findAll(message).forEach { match ->
            val range = match.range
            // Append any text before the matched part.
            if (range.first > lastIndex) {
                val plainText = message.substring(lastIndex, range.first)
                baseComponent.addExtra(TextComponent(plainText))
            }
            // Extract the command from within the single quotes.
            val commandText = match.groupValues[1].trim()
            // Create a clickable component for the command.
            val clickableComponent = TextComponent(commandText)
            clickableComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, commandText)
            baseComponent.addExtra(clickableComponent)
            lastIndex = range.last + 1
        }

        // Append any remaining text after the last match.
        if (lastIndex < message.length) {
            baseComponent.addExtra(TextComponent(message.substring(lastIndex)))
        }

        return baseComponent
    }


    fun applyPlaceholders(message: String, placeholder: CodePlaceHolder, isPlaceholderHooked: () -> Boolean = { false }): String {
        val placeholders: Map<String, String> = mapOf(
            "player" to placeholder.sender.name,
            "args" to placeholder.args.joinToString(" "),
            "property" to placeholder.property,

            "code" to placeholder.code,
            "total_code" to placeholder.totalCodes.toString(),
            "status" to placeholder.status,

            "cooldown" to placeholder.cooldown,
            "duration" to placeholder.duration,
            "expiry" to placeholder.validTo,
            "expired" to placeholder.isExpired,

            "total_redemption" to placeholder.totalRedemption,
            "player_redeemed" to placeholder.totalPlayerUsage,
            "max_player_limit" to placeholder.playerLimit,
            "max_redemption" to placeholder.redemptionLimit,

            "permission" to placeholder.permission,
            "pin" to placeholder.pin,

            "command" to placeholder.command,
            "id" to placeholder.commandId,

            "target" to placeholder.target,
            "usedBy" to placeholder.usedBy,
            "redeemed_by" to placeholder.redeemedBy,

            "template" to placeholder.template,
            "sync" to placeholder.templateSync,
            "sound" to placeholder.sound,

            "min" to placeholder.minLength,
            "max" to placeholder.maxLength,
            "digit" to placeholder.codeGenerateDigit,

            )
        val text = placeholders.entries.fold(message) { msg, (placeholder, value) ->
            msg.replace("{$placeholder}", value)
        }

        return if (placeholder.sender is Player && isPlaceholderHooked()) PlaceholderAPI.setPlaceholders(placeholder.sender as Player, text) else text
    }
}