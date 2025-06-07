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

package me.justlime.redeemcodex.models

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.command.CommandSender

data class CodePlaceHolder(
    var sender: CommandSender,
    val args: List<String> = emptyList(),
    var sentMessage: String = "",

    var code: String = "none",
    var totalCodes: Int = 1,
    var template: String = "none",
    var templateSync: String = "none",
    var command: String = "none",
    var commandId: String = "none",
    var duration: String = "none",
    var status: String = "none",

    var permission: String = "none",
    var requiredPermission: String = "none",
    var pin: String = "none",
    var target: String = "none",
    var cooldown: String = "none",
    val isExpired: String = "none",
    var minLength: String = "none",
    var maxLength: String = "none",
    var codeGenerateDigit: String = "none",
    var property: String = "none",

    var redemptionLimit: String = "none",
    var playerLimit: String = "none",
    var usedBy: String = "none",
    var redeemedBy: String = "none",
    var totalPlayerUsage: String = "none",
    var totalRedemption: String = "none",


    var validTo: String = "none",
    var validFrom: String = "none",
    var lastRedeemed: String = "none",
    var sound: String = "None",

    var chatMessage: String = ""
) {
    companion object {
        fun fetchByDB(plugin: RedeemCodeX, code: String, sender: CommandSender): CodePlaceHolder {
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
                status = redeemCode.enabledStatus.toString(),
                redemptionLimit = redeemCode.redemption.toString(),
                playerLimit = redeemCode.playerLimit.toString(),
                permission = redeemCode.permission,
                pin = if (redeemCode.pin <= 0) "none" else redeemCode.pin.toString(),
                target = redeemCode.target.toString(),
                usedBy = redeemCode.usedBy.toString(),
                template = redeemCode.template,
                templateSync = redeemCode.sync.toString(),
                cooldown = redeemCode.cooldown,
                isExpired = JService.isExpired(redeemCode).toString(),
                minLength = plugin.configRepo.getConfigValue("code-minimum-digit"),
                maxLength = plugin.configRepo.getConfigValue("code-maximum-digit"),
                codeGenerateDigit = plugin.configRepo.getConfigValue("default.code-generate-digit"),
                sound = redeemCode.sound.sound.toString(),
            )
        } //TODO Remove it

        fun applyByRedeemCode(redeemCode: RedeemCode, sender: CommandSender): CodePlaceHolder {
            return CodePlaceHolder(
                sender = sender,
                code = redeemCode.code,
                template = redeemCode.template,
                command = redeemCode.commands.toString().removeSurrounding("{", "}").trim(),
                duration = redeemCode.duration,
                status = redeemCode.enabledStatus.toString(),
                redemptionLimit = redeemCode.redemption.toString(),
                playerLimit = redeemCode.playerLimit.toString(),
                permission = redeemCode.permission,
                pin = if (redeemCode.pin <= 0) "none" else redeemCode.pin.toString(),
                target = redeemCode.target.toString(),
                usedBy = redeemCode.usedBy.map {
                    "${it.key} = ${it.value}"
                }.joinToString(", "),
                templateSync = redeemCode.sync.toString(),
                cooldown = redeemCode.cooldown,
                minLength = "none",
                maxLength = "none",
                codeGenerateDigit = "6",
            )
        }

        fun applyByTemplate(template: RedeemTemplate, sender: CommandSender): CodePlaceHolder {
            return CodePlaceHolder(
                sender = sender,
                template = template.name,
                status = template.defaultEnabledStatus.toString(),
                templateSync = template.defaultSync.toString(),

                duration = template.duration,
                cooldown = template.cooldown,

                redemptionLimit = template.redemption.toString(),
                playerLimit = template.playerLimit.toString(),

                permission = template.permissionValue,
                pin = if (template.pin <= 0) "none" else template.pin.toString(),
                minLength = "none",
                maxLength = "none",
                codeGenerateDigit = "none",
                command = template.commands.toString().removeSurrounding("{", "}").trim()
            )
        }

    }
}