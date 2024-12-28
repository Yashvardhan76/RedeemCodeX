package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.enums.JTab
import me.justlime.redeemX.enums.JTemplate
import me.justlime.redeemX.gui.EditMessages
import me.justlime.redeemX.gui.EditRewards
import me.justlime.redeemX.gui.EditSound
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.utilities.JService
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ModifySubCommand(private val plugin: RedeemX) : JSubCommand {
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)

    private fun editor(redeemCode: RedeemCode, value: String, placeHolder: CodePlaceHolder, sender: CommandSender){
        if (sender !is Player) return
        when (value) {
            JTab.Modify.Edit.Reward.value -> {
                EditRewards.code(plugin, redeemCode, sender)
                sender.sendMessage("Editing")
                config.sendMsg(JMessage.RCX.Modify.Edit.REWARDS, placeHolder)
            }
            JTab.Modify.Edit.Message.value -> {
               EditMessages.code(plugin, redeemCode)
                config.sendMsg(JMessage.RCX.Modify.Edit.MESSAGE, placeHolder)
            }
            JTab.Modify.Edit.Sound.value -> {
                EditSound.code(plugin, redeemCode)
                config.sendMsg(JMessage.RCX.Modify.Edit.SOUND, placeHolder)
            }
        }
        update(redeemCode, placeHolder)
    }

    private fun getUsage(placeHolder: CodePlaceHolder): Boolean {
        //TODO Remove Usages From Modify and Shift to usage subcommand
        config.sendMsg(JMessage.RCX.Modify.LIST, placeHolder)
        return true
    }

    private fun toggle(redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        codeRepo.toggleEnabled(redeemCode)
        placeHolder.isEnabled = redeemCode.enabled.toString()
        config.sendMsg(JMessage.RCX.Modify.ENABLED, placeHolder)
        return update(redeemCode, placeHolder)
    }

    private fun setPermission(redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        if (redeemCode.permission.isNotBlank()) {
            codeRepo.setPermission(redeemCode, "")
            placeHolder.permission = redeemCode.permission
            config.sendMsg(JMessage.RCX.Modify.UNSET_PERMISSION, placeHolder)
        } else {
            val permission = config.getTemplateValue("default", JTemplate.PERMISSION_VALUE.property)
            codeRepo.setPermission(redeemCode, permission)
            placeHolder.permission = redeemCode.permission
            config.sendMsg(JMessage.RCX.Modify.SET_PERMISSION, placeHolder)
        }
        return update(redeemCode, placeHolder)
    }

    private fun setLocked(redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        placeHolder.templateLocked = redeemCode.locked.toString()
        if (redeemCode.template.isBlank()) return config.sendMsg(
            JMessage.RCX.Modify.TEMPLATE_EMPTY, placeHolder
        ) != Unit
        codeRepo.setTemplateLocked(redeemCode, !redeemCode.locked)
        config.sendMsg(JMessage.RCX.Modify.TEMPLATE_LOCKED, placeHolder)
        return update(redeemCode, placeHolder)
    }

    private fun getList(redeemCode: RedeemCode, sender: CommandSender): Boolean {
        val targetList = redeemCode.target.joinToString(", ")
        sender.sendMessage(targetList)
        return true
    }


    private fun setTemplate(redeemCode: RedeemCode, template: String, placeHolder: CodePlaceHolder): Boolean {
        redeemCode.template = template
        placeHolder.template = template
        val templateState = config.getTemplate(redeemCode.template) ?: config.getTemplate("default") ?: return config.sendMsg(
            JMessage.RCX.Modify.TEMPLATE_INVALID, placeHolder
        ) != Unit
        codeRepo.templateToRedeemCode(redeemCode, templateState, false)
        return update(redeemCode, placeHolder)
    }

    private fun setTarget(redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        placeHolder.target = ""
        codeRepo.clearTarget(redeemCode)
        config.sendMsg(JMessage.RCX.Modify.Target.REMOVE_ALL, placeHolder)
        return update(redeemCode, placeHolder)
    }

    private fun modifyDuration(
        code: RedeemCode, existingDuration: String, duration: String, isAdding: Boolean, placeHolder: CodePlaceHolder
    ): Boolean {
        if (!JService.isDurationValid(duration)) return config.sendMsg(JMessage.RCX.Modify.INVALID_VALUE, placeHolder) != Unit
        code.duration = JService.adjustDuration(existingDuration, duration, isAdding)
        placeHolder.duration = code.duration
        config.sendMsg(JMessage.RCX.Modify.DURATION, placeHolder)
        return true
    }

    private fun setCooldown(redeemCode: RedeemCode, duration: String, placeHolder: CodePlaceHolder): Boolean {
        placeHolder.cooldown = duration
        if (!codeRepo.setCooldown(redeemCode, duration)) return config.sendMsg(JMessage.RCX.Modify.INVALID_VALUE, placeHolder) != Unit
        config.sendMsg(JMessage.RCX.Modify.COOLDOWN, placeHolder)
        return true
    }

    private fun update(redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        redeemCode.modified = JService.getCurrentTime()
        val success = codeRepo.upsertCode(redeemCode)
        if (!success) {
            config.sendMsg(JMessage.RCX.Modify.FAILED, placeHolder)
            return false
        }
        return true
    }

    override var codeList: List<String> = emptyList()
    override val permission: String = ""

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        if (!hasPermission(sender)) {
            config.sendMsg(JMessage.NO_PERMISSION, CodePlaceHolder(sender, args))
            return true
        }
        if (args.size < 4) return config.sendMsg(
            JMessage.RCX.Help.UNKNOWN_COMMAND, CodePlaceHolder(sender, args)
        ) != Unit
        val placeHolder = CodePlaceHolder.fetchByDB(plugin, args[2].uppercase(), sender)
        placeHolder.property = args[3]
        val redeemCode = codeRepo.getCode(args[2].uppercase())
        codeList = listOf(args[2])
        if (redeemCode == null) {
            config.sendMsg(JMessage.RCX.Modify.NOT_FOUND, placeHolder)
            return false
        }
        when (placeHolder.property) {
            JTab.GeneralActions.Usage.value -> return getUsage(placeHolder)
            JTab.Modify.Enabled.value -> return toggle(redeemCode, placeHolder)
            JTab.Modify.Locked.value -> return setLocked(redeemCode, placeHolder)
            JTab.Modify.ListTarget.value -> return getList(redeemCode, sender)
        }

        if (args.size == 4) when (placeHolder.property) {
            JTab.Modify.SetPermission.value -> {
                if (redeemCode.locked) return false
                return setPermission(redeemCode, placeHolder)
            }
            JTab.Modify.SetTarget.value -> return setTarget(redeemCode, placeHolder)
        }

        if (args.size < 5) return config.sendMsg(JMessage.RCX.Help.UNKNOWN_COMMAND, placeHolder) != Unit
        val value = args[4]
        when (placeHolder.property) {
            JTab.Modify.SetTemplate.value -> setTemplate(redeemCode, value, placeHolder)

            JTab.Modify.SetTarget.value -> {
                if (args.size < 5) return config.sendMsg(JMessage.RCX.Help.UNKNOWN_COMMAND, placeHolder) != Unit
                placeHolder.target = value
                codeRepo.setTarget(redeemCode, args.drop(4))
                config.sendMsg(JMessage.RCX.Modify.Target.SET, placeHolder)
            }

            JTab.Modify.AddTarget.value -> {
                if (args.size < 5) return config.sendMsg(JMessage.RCX.Help.UNKNOWN_COMMAND, placeHolder) != Unit
                placeHolder.target = value
                codeRepo.addTarget(redeemCode, value)
                config.sendMsg(JMessage.RCX.Modify.Target.ADD, placeHolder)
            }

            JTab.Modify.RemoveTarget.value -> {
                if (args.size < 5) return config.sendMsg(JMessage.RCX.Help.UNKNOWN_COMMAND, placeHolder) != Unit
                placeHolder.target = value
                codeRepo.removeTarget(redeemCode, value)
                config.sendMsg(JMessage.RCX.Modify.Target.REMOVE, placeHolder)
            }

            JTab.GeneralActions.Usage.value -> {
                sender.sendMessage(redeemCode.target.joinToString("\n"))
            }
        }
        if (!redeemCode.locked)
        when (placeHolder.property) {

            JTab.Modify.Edit.It.value -> {
                editor(redeemCode, value, placeHolder, sender)
                return true
            }

            JTab.Modify.SetDuration.value -> modifyDuration(redeemCode, "0s", value, true, placeHolder)

            JTab.Modify.AddDuration.value -> modifyDuration(redeemCode, redeemCode.duration, value, true, placeHolder)

            JTab.Modify.RemoveDuration.value -> modifyDuration(redeemCode, redeemCode.duration, value, false, placeHolder)

            JTab.Modify.Cooldown.value -> setCooldown(redeemCode, value, placeHolder)

            JTab.Modify.SetRedemption.value -> {
                placeHolder.redemptionLimit = value
                if (!codeRepo.setMaxRedeems(redeemCode, value.toIntOrNull() ?: 1)) return config.sendMsg(
                    JMessage.RCX.Modify.INVALID_VALUE, placeHolder
                ) != Unit
                codeRepo.setMaxRedeems(redeemCode, value.toIntOrNull() ?: 1)
                config.sendMsg(JMessage.RCX.Modify.MAX_REDEEMS, placeHolder)
            }

            JTab.Modify.SetPlayerLimit.value -> {
                placeHolder.playerLimit = value
                if (!codeRepo.setMaxPlayers(
                        redeemCode, value.toIntOrNull() ?: 1
                    )
                ) return config.sendMsg(JMessage.RCX.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setMaxPlayers(redeemCode, value.toIntOrNull() ?: 1)
                config.sendMsg(JMessage.RCX.Modify.MAX_PLAYERS, placeHolder)
            }

            JTab.Modify.SetPermission.value -> {
                placeHolder.permission = value
                if (!codeRepo.setPermission(
                        redeemCode, value
                    )
                ) return config.sendMsg(JMessage.RCX.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setPermission(redeemCode, value)
                config.sendMsg(JMessage.RCX.Modify.SET_PERMISSION, placeHolder)
            }

            JTab.Modify.SetPin.value -> {
                placeHolder.pin = value
                if (!codeRepo.setPin(
                        redeemCode, value.toIntOrNull() ?: 0
                    )
                ) return config.sendMsg(JMessage.RCX.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setPin(redeemCode, value.toIntOrNull() ?: 0)
                config.sendMsg(JMessage.RCX.Modify.PIN, placeHolder)
            }

            JTab.Modify.SetCommand.value -> {
                if (args.size < 5) return config.sendMsg(JMessage.RCX.Modify.INVALID_COMMAND, placeHolder) != Unit
                val commands = args.drop(5).joinToString(" ")
                placeHolder.command = commands
                if (commands.isBlank()) return config.sendMsg(JMessage.RCX.Modify.INVALID_COMMAND, placeHolder) != Unit
                val id = value.toIntOrNull() ?: return config.sendMsg(JMessage.RCX.Modify.INVALID_ID, placeHolder) != Unit
                redeemCode.commands[id] = commands
                sender.sendMessage(placeHolder.command)
                config.sendMsg(JMessage.RCX.Modify.SUCCESS, placeHolder)
            }

            JTab.Modify.AddCommand.value -> {
                val command = args.drop(4).joinToString(" ")
                placeHolder.command = command
                if (command.isBlank()) return config.sendMsg(
                    JMessage.RCX.Modify.INVALID_COMMAND, placeHolder
                ) != Unit
                redeemCode.commands.add(command)
                config.sendMsg(JMessage.RCX.Modify.SUCCESS, placeHolder)
            }

            JTab.Modify.RemoveCommand.value -> {
                placeHolder.commandId = value
                val id = value.toIntOrNull() ?: return config.sendMsg(
                    JMessage.RCX.Modify.INVALID_ID, placeHolder
                ) != Unit
                redeemCode.commands.removeAt(id)
                config.sendMsg(JMessage.RCX.Modify.SUCCESS, placeHolder)
            }

            JTab.Modify.ListCommand.value -> {
                val commandsList = redeemCode.commands.joinToString("\n")
                sender.sendMessage(commandsList)
                config.sendMsg(JMessage.RCX.Modify.LIST, placeHolder)
            }


            else -> {
                config.sendMsg(JMessage.RCX.Modify.UNKNOWN_PROPERTY, placeHolder)
                return false
            }

        }
        else return config.sendMsg(JMessage.RCX.Modify.LOCKED, placeHolder) != Unit
        // Save updated redeem code
        return update(redeemCode, placeHolder)
    }
}
