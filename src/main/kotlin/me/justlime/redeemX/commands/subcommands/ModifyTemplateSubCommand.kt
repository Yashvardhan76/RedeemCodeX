package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.yml.JMessage
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class ModifyTemplateSubCommand(plugin: RedeemX): JSubCommand {
    private val config = plugin.config
    private val codeRepo = plugin.codeRepository
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        if (args.size < 3) return config.sendMsg(JMessage.Commands.ModifyTemplate.INVALID_SYNTAX, CodePlaceHolder(sender, args)) != Unit
        val placeHolder = CodePlaceHolder(sender, args, template = args[1], property = args[2])
        val property = args[2].lowercase()
        val template = config.getTemplate(args[1]) ?: return config.sendMsg(JMessage.Commands.ModifyTemplate.NOT_FOUND, placeHolder) != Unit
        if (args.size < 4) return config.sendMsg(JMessage.Commands.ModifyTemplate.INVALID_SYNTAX, placeHolder) != Unit
        val value = args[3]
        when (property) {
            "enabled" -> {
                template.isEnabled = !template.isEnabled
                config.sendMsg(JMessage.Commands.ModifyTemplate.ENABLED, placeHolder)
            }

            "max_redeems" -> {
                template.maxRedeems = value.toIntOrNull() ?: 0
                if (template.maxRedeems < 1) return config.sendMsg(JMessage.Commands.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                config.sendMsg(JMessage.Commands.ModifyTemplate.MAX_REDEEMS, placeHolder)
            }

            "max_player" -> {
                template.maxPlayers = value.toIntOrNull() ?: 0
                if (template.maxPlayers < 1) return config.sendMsg(JMessage.Commands.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                config.sendMsg(JMessage.Commands.ModifyTemplate.MAX_PLAYERS, placeHolder)
            }

            "permission" -> {
                template.permissionRequired = value.equals("true", ignoreCase = true)
                template.permissionValue = if (!value.equals("false", ignoreCase = true)) value else ""
                config.sendMsg(JMessage.Commands.ModifyTemplate.PERMISSION, placeHolder)
            }

            "pin" -> {
                template.pin = value.toIntOrNull() ?: 0
                if (template.pin < 0) return config.sendMsg(JMessage.Commands.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                config.sendMsg(JMessage.Commands.ModifyTemplate.PIN, placeHolder)
            }

            "code_generate_digit" -> {
                template.codeGenerateDigit = value.toIntOrNull() ?: 0

                return false
            }
        }
        return true
    }
}
