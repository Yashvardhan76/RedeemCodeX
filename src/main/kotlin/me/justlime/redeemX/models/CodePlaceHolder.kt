package me.justlime.redeemX.models

import me.justlime.redeemX.RedeemX
import org.bukkit.command.CommandSender

data class CodePlaceHolder(
    val sender: CommandSender,
    val args: List<String> = emptyList(),
    var code: String = "none",
    var template: String = "none",
    var command: String = "none",
    var commandId: String = "none",
    var duration: String = "none",
    var isEnabled: String = "none",
    var redemption: String = "none",
    var playerLimit: String = "none",
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
    var property: String = "none",
    var player: String = "none",
) {

    companion object {
        fun fetchByDB(plugin: RedeemX, code: String, sender: CommandSender): CodePlaceHolder {
            val redeemCode = plugin.redeemCodeDB.get(code) ?: return CodePlaceHolder(sender, code = code)

            val durationSeconds = redeemCode.duration.removeSuffix("s").toIntOrNull() ?: 0
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
                command = redeemCode.commands.toString().removeSurrounding("{", "}").trim(),
                duration = if (redeemCode.duration.isEmpty()) "none" else formattedDuration,
                isEnabled = redeemCode.enabled.toString(),
                redemption = redeemCode.redemption.toString(),
                playerLimit = redeemCode.limit.toString(),
                permission = redeemCode.permission,
                pin = if (redeemCode.pin <= 0) "none" else redeemCode.pin.toString(),
                target = redeemCode.target.toString(),
                usage = redeemCode.usedBy.toString(),
                template = redeemCode.template,
                templateLocked = redeemCode.locked.toString(),
                cooldown = redeemCode.cooldown,
                isExpired = plugin.service.isExpired(redeemCode).toString(),
                minLength = plugin.config.getConfigValue("code-minimum-digit"),
                maxLength = plugin.config.getConfigValue("code-maximum-digit"),
                codeGenerateDigit = plugin.config.getConfigValue("default.code-generate-digit")
            )
        }
    }
}