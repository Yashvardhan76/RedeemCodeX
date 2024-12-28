package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.enums.JTab
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.models.RedeemTemplate
import me.justlime.redeemX.utilities.JService
import org.bukkit.command.CommandSender

class ModifyTemplateSubCommand(plugin: RedeemX) : JSubCommand {
    private val config = plugin.config
    private val codeRepo = RedeemCodeRepository(plugin)
    override var codeList: List<String> = emptyList()
    override val permission: String = ""
    private lateinit var placeHolder: CodePlaceHolder

    private fun upsertTemplate(template: RedeemTemplate): Boolean {
        val success = config.modifyTemplate(template)
        if (upsetCodes(template)) config.sendMsg(JMessage.RCX.ModifyTemplate.CODES_MODIFIED, placeHolder)

        if (success) {
            config.sendMsg(JMessage.RCX.ModifyTemplate.SUCCESS, placeHolder)
            return true
        } else {
            config.sendMsg(JMessage.RCX.ModifyTemplate.FAILED, placeHolder)
            return false
        }
    }

    private fun upsetCodes(template: RedeemTemplate): Boolean {
        val redeemCode: List<RedeemCode> = codeRepo.getCodesByTemplate(template.name, true)
        if (redeemCode.isEmpty()) return false
        val codes: MutableList<String> = mutableListOf()
        redeemCode.forEach { codes.add(it.code); codeRepo.templateToRedeemCode(it, template, template.locked) }
        placeHolder.code = codes.joinToString(" ")

        codeRepo.upsertCodes(redeemCode)
        return true
    }

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        if (!hasPermission(sender)) {
            config.sendMsg(JMessage.NO_PERMISSION, CodePlaceHolder(sender, args))
            return true
        }
        if (args.size < 3) return config.sendMsg(JMessage.RCX.UNKNOWN_COMMAND, CodePlaceHolder(sender)) != Unit
        placeHolder = CodePlaceHolder(sender, args, template = args[2], property = args[3])

        val property = args[3]
        val template = config.getTemplate(args[2]) ?: return config.sendMsg(JMessage.RCX.ModifyTemplate.NOT_FOUND, placeHolder) != Unit
        if (args.size < 5) return config.sendMsg(JMessage.RCX.UNKNOWN_COMMAND, placeHolder) != Unit
        val value = args[4]
        when (property) {
            JTab.Template.SetRedemption.value -> {
                val maxRedeems = value.toIntOrNull() ?: return config.sendMsg(JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                template.redemption = maxRedeems
                upsertTemplate(template)
            }

            JTab.Template.SetPlayerLimit.value -> {
                val maxPlayers = value.toIntOrNull() ?: return config.sendMsg(JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                template.playerLimit = maxPlayers
                upsertTemplate(template)
            }

            JTab.Template.SetPin.value -> {
                val pin = value.toIntOrNull() ?: return config.sendMsg(JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                template.pin = pin
                upsertTemplate(template)
            }

            JTab.Template.SetPermission.value -> {
                template.permissionValue = value
                template.permissionRequired = true
                upsertTemplate(template)
            }

            JTab.Template.TogglePermissionRequired.value -> {
                template.permissionRequired = false
                template.permissionValue = ""
                upsertTemplate(template)
            }

            JTab.Template.SetDuration.value -> {
                if (!JService.isDurationValid(value)) return config.sendMsg(
                    JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder
                ) != Unit
                template.duration = value
                upsertTemplate(template)
            }

            JTab.Template.AddDuration.value -> {
                if (!JService.isDurationValid(value)) return config.sendMsg(
                    JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder
                ) != Unit
                template.duration = JService.adjustDuration(template.duration, value, true)
                upsertTemplate(template)
            }

            JTab.Template.RemoveDuration.value -> {
                if (!JService.isDurationValid(value)) return config.sendMsg(
                    JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder
                ) != Unit
                template.duration = JService.adjustDuration(template.duration, value, false)
                upsertTemplate(template)
            }


            JTab.Template.SetCooldown.value -> {
                if (!JService.isDurationValid(value)) return config.sendMsg(
                    JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder
                ) != Unit
                template.cooldown = value
                upsertTemplate(template)
            }

            JTab.Template.SetCommand.value -> {
                if (value.isBlank()) return config.sendMsg(JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                val id = value.toIntOrNull() ?: return config.sendMsg(JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                if (id>template.commands.size) return config.sendMsg(JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                template.commands[id] = args.drop(5).joinToString(" ")
                upsertTemplate(template)
            }

            JTab.Template.AddCommand.value -> {
                if (value.isBlank()) return config.sendMsg(JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                template.commands.add(args.drop(4).joinToString(" "))
                upsertTemplate(template)
            }

            JTab.Template.RemoveCommand.value -> {
                val id = value.toIntOrNull() ?: return config.sendMsg(JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                if (id>template.commands.size) return config.sendMsg(JMessage.RCX.ModifyTemplate.INVALID_VALUE, placeHolder) != Unit
                template.commands.removeAt(id)
                upsertTemplate(template)
            }

            JTab.Template.ListCommand.value -> {
                val commands = template.commands.withIndex().joinToString("\n")
                placeHolder.sender.sendMessage(commands)
                return true
            }

            JTab.Template.Locked.value -> {
                template.locked = value.toBoolean()
                upsertTemplate(template)
            }

            else -> {
                config.sendMsg(JMessage.RCX.UNKNOWN_COMMAND, placeHolder)
                return false
            }
        }
        return true
    }
}
