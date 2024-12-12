package me.justlime.redeemX.models

import me.justlime.redeemX.RedeemX
import org.bukkit.command.CommandSender
import java.time.format.DateTimeFormatter

data class CodePlaceHolder(val sender: CommandSender,
                           val args: List<String> = emptyList(),
                           var code: String = "none",
                           var template: String = "none",
                           var command: String = "none",
                           var commandId: String = "none",
                           var duration: String = "none",
                           var isEnabled: String = "none",
                           var maxRedeemsPerPlayer: String = "none",
                           var maxPlayersCanRedeem: String = "none",
                           var permission: String = "none",
                           var pin: String = "none",
                           var target: String = "none",
                           var usage: String = "none",
                           var templateLocked: String = "none",
                           var cooldown: String = "none",
                           val isExpired: String = "none",
                           var minLength: String = "none",
                           var maxLength: String = "none",
                           var codeGenerateDigit: String = "none",
                           var property: String = "none"
) {

    companion object {
        fun fetchByDB(plugin: RedeemX, code: String, sender: CommandSender): CodePlaceHolder {
            val db = plugin.redeemCodeDB.get(code) ?: return CodePlaceHolder(sender, code = code)

            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val durationSeconds = db.duration.orEmpty().removeSuffix("s").toIntOrNull() ?: 0
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

            return CodePlaceHolder(
                sender = sender,
                code = code,
                command = db.commands.toString().removeSurrounding("{", "}").trim(),
                duration = if (db.duration.isNullOrEmpty()) "none" else formattedDuration,
                isEnabled = db.isEnabled.toString(),
                maxRedeemsPerPlayer = db.maxRedeems.toString(),
                maxPlayersCanRedeem = db.maxPlayers.toString(),
                permission = db.permission ?: "none",
                pin = if (db.pin <= 0) "none" else db.pin.toString(),
                target = db.target.toString(),
                usage = db.usage.toString(),
                template = db.template,
                templateLocked = db.templateLocked.toString(),
                cooldown = db.cooldown ?: "none",
                isExpired = plugin.service.isExpired(code).toString(),
                minLength = plugin.config.getConfigValue("code-minimum-digit"),
                maxLength = plugin.config.getConfigValue("code-maximum-digit"),
                codeGenerateDigit = plugin.config.getConfigValue("default.code-generate-digit")?: "none"
            )
        }
    }
}