package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.command.CommandSender

class ModifyTemplateSubCommand(plugin: RedeemX) : JSubCommand {
    private val config = plugin.config
    private val codeRepo = RedeemCodeRepository(plugin)
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        if (args.size < 3) return config.sendMsg(JMessage.Commands.ModifyTemplate.INVALID_SYNTAX, CodePlaceHolder(sender)) != Unit
        val placeHolder = CodePlaceHolder(sender, args, template = args[1], property = args[2])
        val property = args[2].lowercase()
        val template = config.getTemplate(args[1]) ?: return config.sendMsg(JMessage.Commands.ModifyTemplate.NOT_FOUND, placeHolder) != Unit
        if (args.size < 4) return config.sendMsg(JMessage.Commands.ModifyTemplate.INVALID_SYNTAX, placeHolder) != Unit
        val value = args[3]
        when {

            property.equals("maxRedeems", ignoreCase = true) -> {
                template.maxRedeems = value.toIntOrNull() ?: 1
                if (template.maxRedeems < 1) return config.sendMsg(JMessage.Commands.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                config.sendMsg(JMessage.Commands.ModifyTemplate.MAX_REDEEMS, placeHolder)
            }

            property.equals("maxPlayers", ignoreCase = true) -> {
                template.maxPlayers = value.toIntOrNull() ?: 1
                if (template.maxPlayers < 1) return config.sendMsg(JMessage.Commands.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                config.sendMsg(JMessage.Commands.ModifyTemplate.MAX_PLAYERS, placeHolder)
            }

            property.equals("permission", ignoreCase = true) -> {
                template.permissionValue = value
                config.sendMsg(JMessage.Commands.ModifyTemplate.PERMISSION, placeHolder)
            }

            property.equals("permissionRequired", ignoreCase = true) -> {
                template.permissionRequired = value.toBooleanStrictOrNull() ?: false
                config.sendMsg(JMessage.Commands.ModifyTemplate.PERMISSION, placeHolder)
            }

            property.equals("setPin", ignoreCase = true) -> {
                template.pin = value.toIntOrNull() ?: 0
                config.sendMsg(JMessage.Commands.ModifyTemplate.PIN, placeHolder)
            }

            property.equals("setDuration", ignoreCase = true) -> {
                template.duration = value
                config.sendMsg(JMessage.Commands.ModifyTemplate.DURATION, placeHolder)
            }

            property.equals("addDuration", ignoreCase = true) -> {
                template.duration = RedeemCodeService().adjustDuration(template.duration, value, true)
                config.sendMsg(JMessage.Commands.ModifyTemplate.DURATION, placeHolder)
            }

            property.equals("removeDuration", ignoreCase = true) -> {
                template.duration = RedeemCodeService().adjustDuration(template.duration, value, false)
                config.sendMsg(JMessage.Commands.ModifyTemplate.DURATION, placeHolder)
            }

            property.equals("setDigit", ignoreCase = true) -> {
                template.codeGenerateDigit = value.toIntOrNull() ?: 4
                if (template.codeGenerateDigit < 1 || template.codeGenerateDigit > 10) return config.sendMsg(JMessage.Commands.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                config.sendMsg(JMessage.Commands.ModifyTemplate.CODE_GENERATE_DIGIT, placeHolder)
            }

            property.equals("setCooldown", ignoreCase = true) -> {
                template.cooldown = value
                config.sendMsg(JMessage.Commands.ModifyTemplate.COOLDOWN, placeHolder)
            }

            property.equals("commands", ignoreCase = true) -> {}

            else -> {
                config.sendMsg(JMessage.Commands.ModifyTemplate.UNKNOWN_PROPERTY, placeHolder)
                return false
            }
        }
        val success = config.modifyTemplate(template)
        if (success) {
            config.sendMsg(JMessage.Commands.ModifyTemplate.SUCCESS, placeHolder)
            return true
        } else {
            config.sendMsg(JMessage.Commands.ModifyTemplate.FAILED, placeHolder)
            return false
        }
    }
}
