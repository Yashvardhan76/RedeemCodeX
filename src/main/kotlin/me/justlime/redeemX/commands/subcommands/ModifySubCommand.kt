package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.enums.JTemplate
import me.justlime.redeemX.enums.Tab
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.command.CommandSender

class ModifySubCommand(private val plugin: RedeemX) : JSubCommand {
    private val service: RedeemCodeService = plugin.service
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    private val console = plugin.server.consoleSender

    private fun getUsage(placeHolder: CodePlaceHolder): Boolean {
        //TODO Remove Usages From Modify and Shift to usage subcommand
        config.sendMsg(JMessage.Commands.Modify.LIST, placeHolder)
        return true
    }

    private fun toggle(redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        codeRepo.toggleEnabled(redeemCode)
        placeHolder.isEnabled = redeemCode.enabled.toString()
        config.sendMsg(JMessage.Commands.Modify.ENABLED, placeHolder)
        return update(redeemCode, placeHolder)
    }

    private fun setPermission(redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        if (redeemCode.permission.isNotBlank()) {
            codeRepo.setPermission(redeemCode, "")
            placeHolder.permission = redeemCode.permission
            config.sendMsg(JMessage.Commands.Modify.UNSET_PERMISSION, placeHolder)
        } else {
            val permission = config.getTemplateValue("default", JTemplate.PERMISSION_VALUE.property)
            codeRepo.setPermission(redeemCode, permission)
            placeHolder.permission = redeemCode.permission
            config.sendMsg(JMessage.Commands.Modify.SET_PERMISSION, placeHolder)
        }
        return update(redeemCode, placeHolder)
    }

    private fun setLocked(redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        placeHolder.templateLocked = redeemCode.locked.toString()
        if (redeemCode.template.isBlank()) return config.sendMsg(
            JMessage.Commands.Modify.TEMPLATE_EMPTY, placeHolder
        ) != Unit
        codeRepo.setTemplateLocked(redeemCode, !redeemCode.locked)
        config.sendMsg(JMessage.Commands.Modify.TEMPLATE_LOCKED, placeHolder)
        return update(redeemCode, placeHolder)
    }

    private fun getList(redeemCode: RedeemCode, sender: CommandSender): Boolean {
        val targetList = redeemCode.target.joinToString(", ")
        sender.sendMessage(targetList)
        return true
    }

    private fun previewCommand(redeemCode: RedeemCode): Boolean {
        redeemCode.commands.values.forEach { plugin.server.dispatchCommand(console, it) }
        return true
    }

    private fun setTemplate(redeemCode: RedeemCode, template: String, placeHolder: CodePlaceHolder): Boolean {
        redeemCode.template = template
        placeHolder.template = template
        val templateState = config.getTemplate(redeemCode.template) ?: config.getTemplate("default") ?: return config.sendMsg(
            JMessage.Commands.Modify.TEMPLATE_INVALID, placeHolder
        ) != Unit
        codeRepo.templateToRedeemCode(redeemCode, templateState, false)
        return update(redeemCode, placeHolder)
    }

    private fun setTarget(redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        placeHolder.target = ""
        codeRepo.clearTarget(redeemCode)
        config.sendMsg(JMessage.Commands.Modify.Target.REMOVE_ALL, placeHolder)
        return update(redeemCode, placeHolder)
    }

    private fun modifyDuration(code: RedeemCode, existingDuration: String, duration: String, isAdding: Boolean, placeHolder: CodePlaceHolder
    ): Boolean {
        if (!service.isDurationValid(duration)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
        code.duration = service.adjustDuration(existingDuration, duration, isAdding)
        config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)
        return true
    }

    private fun setCooldown(redeemCode: RedeemCode, duration: String, placeHolder: CodePlaceHolder): Boolean {
        placeHolder.cooldown = duration
        if (!codeRepo.setCooldown(redeemCode, duration)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
        config.sendMsg(JMessage.Commands.Modify.COOLDOWN, placeHolder)
        return true
    }

    private fun update(redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        val success = codeRepo.upsertCode(redeemCode)
        if (success) {
            config.sendMsg(JMessage.Commands.Modify.SUCCESS, placeHolder)
            return true
        }
        config.sendMsg(JMessage.Commands.Modify.FAILED, placeHolder)
        return false
    }

    override var codeList: List<String> = emptyList()
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        if (args.size < 3) return config.sendMsg(
            JMessage.Commands.Modify.INVALID_SYNTAX, CodePlaceHolder(sender, args)
        ) != Unit
        val placeHolder = CodePlaceHolder.fetchByDB(plugin, args[1], sender)
        placeHolder.property = args[2]
        val redeemCode = codeRepo.getCode(args[1])
       codeList = listOf(args[1])
        if (redeemCode == null) {
            config.sendMsg(JMessage.Commands.Modify.NOT_FOUND, placeHolder)
            return false
        }
        when (placeHolder.property) {
            Tab.GeneralActions.Usage.value -> return getUsage(placeHolder)
            Tab.Modify.Enabled.value -> return toggle(redeemCode, placeHolder)
            Tab.Modify.Locked.value -> return setLocked(redeemCode, placeHolder)
            Tab.Modify.ListTarget.value -> return getList(redeemCode, sender)
            Tab.Modify.PreviewCommand.value -> return previewCommand(redeemCode)
        }

        if (args.size == 3) when (placeHolder.property) {
            Tab.Modify.SetPermission.value -> return setPermission(redeemCode, placeHolder)
            Tab.Modify.SetTarget.value -> return setTarget(redeemCode, placeHolder)
        }

        if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
        val value = args[3]
        when (placeHolder.property) {
            Tab.Modify.SetTemplate.value -> setTemplate(redeemCode, value, placeHolder)

            Tab.Modify.SetDuration.value -> modifyDuration(redeemCode, "0s", value, true, placeHolder)

            Tab.Modify.AddDuration.value -> modifyDuration(redeemCode, redeemCode.duration, value, true, placeHolder)

            Tab.Modify.RemoveDuration.value -> modifyDuration(redeemCode, redeemCode.duration, value, false, placeHolder)

            Tab.Modify.Cooldown.value -> setCooldown(redeemCode, value, placeHolder)

            Tab.Modify.SetRedemption.value -> {
                placeHolder.redemption = value
                if (!codeRepo.setMaxRedeems(redeemCode, value.toIntOrNull() ?: 1)) return config.sendMsg(
                    JMessage.Commands.Modify.INVALID_VALUE,
                    placeHolder
                ) != Unit
                codeRepo.setMaxRedeems(redeemCode, value.toIntOrNull() ?: 1)
                config.sendMsg(JMessage.Commands.Modify.MAX_REDEEMS, placeHolder)
            }

            Tab.Modify.SetPlayerLimit.value -> {
                placeHolder.playerLimit = value
                if (!codeRepo.setMaxPlayers(
                        redeemCode, value.toIntOrNull() ?: 1
                    )
                ) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setMaxPlayers(redeemCode, value.toIntOrNull() ?: 1)
                config.sendMsg(JMessage.Commands.Modify.MAX_PLAYERS, placeHolder)
            }

            Tab.Modify.SetPermission.value -> {
                placeHolder.permission = value
                if (!codeRepo.setPermission(
                        redeemCode, value
                    )
                ) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setPermission(redeemCode, value)
                config.sendMsg(JMessage.Commands.Modify.SET_PERMISSION, placeHolder)
            }

            Tab.Modify.SetPin.value -> {
                placeHolder.pin = value
                if (!codeRepo.setPin(
                        redeemCode, value.toIntOrNull() ?: 0
                    )
                ) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setPin(redeemCode, value.toIntOrNull() ?: 0)
                config.sendMsg(JMessage.Commands.Modify.PIN, placeHolder)
            }

            Tab.Modify.SetTarget.value -> {
                if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                placeHolder.target = value
                codeRepo.setTarget(redeemCode, args.drop(3))
                config.sendMsg(JMessage.Commands.Modify.Target.SET, placeHolder)
            }

            Tab.Modify.AddTarget.value -> {
                if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                placeHolder.target = value
                codeRepo.addTarget(redeemCode, value)
                config.sendMsg(JMessage.Commands.Modify.Target.ADD, placeHolder)
            }

            Tab.Modify.RemoveTarget.value -> {
                if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                placeHolder.target = value
                codeRepo.removeTarget(redeemCode, value)
                config.sendMsg(JMessage.Commands.Modify.Target.REMOVE, placeHolder)
            }

            Tab.Modify.SetCommand.value -> {
                val commands = args.drop(3).joinToString(" ")
                placeHolder.command = commands
                if (commands.isBlank()) return config.sendMsg(
                    JMessage.Commands.Modify.INVALID_COMMAND, placeHolder
                ) != Unit
                codeRepo.setCommands(redeemCode, service.parseToMapId(service.parseToId(commands)))
                sender.sendMessage(placeHolder.command)
                config.sendMsg(JMessage.Commands.Modify.SUCCESS, placeHolder)
            }

            Tab.Modify.AddCommand.value -> {
                val command = args.drop(3).joinToString(" ")
                placeHolder.command = command
                if (command.isBlank()) return config.sendMsg(
                    JMessage.Commands.Modify.INVALID_COMMAND, placeHolder
                ) != Unit
                codeRepo.addCommand(redeemCode, command)
                config.sendMsg(JMessage.Commands.Modify.SUCCESS, placeHolder)
            }

            Tab.Modify.RemoveCommand.value -> {
                placeHolder.commandId = value
                val id = value.toIntOrNull() ?: return config.sendMsg(
                    JMessage.Commands.Modify.INVALID_ID, placeHolder
                ) != Unit
                codeRepo.removeCommand(redeemCode, id)
                config.sendMsg(JMessage.Commands.Modify.SUCCESS, placeHolder)
            }

            Tab.Modify.ListCommand.value -> {
                val commandsList = redeemCode.commands.values.joinToString("\n")
                sender.sendMessage(commandsList)
                config.sendMsg(JMessage.Commands.Modify.LIST, placeHolder)
            }

            Tab.GeneralActions.Usage.value -> {
                sender.sendMessage(redeemCode.target.joinToString("\n"))
            }

            else -> {
                config.sendMsg(JMessage.Commands.Modify.UNKNOWN_PROPERTY, placeHolder)
                return false
            }

        }
        // Save updated redeem code
        val success = codeRepo.upsertCode(redeemCode)
        if (!success) {
            config.sendMsg(JMessage.Commands.Modify.FAILED, placeHolder)
        }
        return success
    }
}
