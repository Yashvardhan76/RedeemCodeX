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
                           var target: String? = null,
                           var inputTarget: String = "",
                           var usage: MutableMap<String, Int> = mutableMapOf(),
                           var usageCount: Int = 0,
                           var property: String = "",
                           var value: String = "",
                           var minLength: Int = 3,
                           var maxLength: Int = 10,
                           var templateName: String = ""

) {
    private val senderName: String = if (sender is Player) sender.name else "console"

    // Converts state data to a map of placeholders and values
    fun toPlaceholdersMap(): Map<String, String> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
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
            "duration" to (duration ?: ""),
            "isEnabled" to if (isEnabled) "Enabled" else "Disabled",
            "maxRedeems" to maxRedeems.toString(),
            "maxPlayers" to maxPlayers.toString(),
            "permission" to (permission ?: ""),
            "pin" to pin.toString(),
            "target" to (target ?: "Any"),
            "usage" to usage.toString(),
            "usageCount" to usageCount.toString(),
            "min" to minLength.toString(),
            "max" to maxLength.toString(),
            "template" to templateName
        )
    }
}
