package me.justlime.redeemX.state

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class RedeemCodeState(var sender: CommandSender,
                           var args: MutableList<String> = mutableListOf(),
                           var code: String = "",
                           var inputCode: String = "",
                           var commands: MutableMap<Int, String> = mutableMapOf(),
                           var inputCommand: String = "",
                           var storedTime: LocalDateTime? = null,
                           var inputStoredTime: String = "",
                           var duration: String? = null,
                           var inputDuration: String = "",
                           var isEnabled: Boolean = false,
                           var inputEnabled: String = "",
                           var maxRedeems: Int = -1,
                           var inputMaxRedeems: String = "",
                           var maxPlayers: Int = -1,
                           var inputMaxPlayers: String = "",
                           var permission: String? = null,
                           var inputPermission: String = "",
                           var hasPermission: Boolean = false,
                           var pin: Int = -1,
                           var inputPin: Int? = null,
                           var target: MutableList<String?> = mutableListOf(),
                           var inputTarget: String = "",
                           var usage: MutableMap<String, Int> = mutableMapOf(),
                           var usageCount: Int = 0,
                           var property: String = "",
                           var value: String = "",
                           var minLength: Int = 3,
                           var maxLength: Int = 10,
                           var template: String = "",
                           var isTemplateLocked: Boolean = false,
                           var inputTemplate: String ="",
                           var storedCooldown: LocalDateTime? = null,
                           var cooldown: String? = null

) {
    private val senderName: String = if (sender is Player) sender.name else "console"

    // Converts state data to a map of placeholders and values
    fun toPlaceholdersMap(): Map<String, String> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val durationSeconds = duration.orEmpty().removeSuffix("s").toIntOrNull() ?: 0
        val days = durationSeconds / 86400
        val hours = (durationSeconds % 86400) / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        val formattedDuration = buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (seconds > 0 || isEmpty()) append("${seconds}s")
        }.trim()

        return mapOf(
            "player" to senderName,
            "code" to inputCode,
            "inputPin" to (inputPin?.toString() ?: "N/A"),
            "inputTarget" to inputTarget,
            "inputCommand" to inputCommand,
            "inputStoredTime" to inputStoredTime,
            "inputDuration" to inputDuration,
            "inputEnabled" to inputEnabled,
            "inputMaxRedeems" to inputMaxRedeems,
            "inputMaxPlayers" to inputMaxPlayers,
            "inputPermission" to inputPermission,
            "valid-code" to code,
            "storedTime" to (storedTime?.format(dateFormatter) ?: "N/A"),
            "duration" to formattedDuration,
            "is_enabled" to if (isEnabled) "Enabled" else "Disabled",
            "max_redeems" to maxRedeems.toString(),
            "max_players" to maxPlayers.toString(),
            "permission" to (permission ?: ""),
            "pin" to pin.toString(),
            "target" to (target.toString()),
            "usage" to usage.toString(),
            "usageCount" to usageCount.toString(),
            "min" to minLength.toString(),
            "max" to maxLength.toString(),
            "template" to template,
            "stored_time" to (storedCooldown?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) ?: "N/A"),
            "cooldown" to (cooldown ?: ""),
            "template_locked" to if (isTemplateLocked) "Locked" else "Unlocked",
            "commands" to commands.map { (id, command) -> "\n[$id] $command" }.joinToString()
)
    }
}
