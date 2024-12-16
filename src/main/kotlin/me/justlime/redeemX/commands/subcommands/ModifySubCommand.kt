package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.enums.JTemplate
import me.justlime.redeemX.enums.Tab
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.command.CommandSender

class ModifySubCommand(private val plugin: RedeemX) : JSubCommand {
    private val service: RedeemCodeService = plugin.service
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        if (args.size < 3) return config.sendMsg(
            JMessage.Commands.Modify.INVALID_SYNTAX,
            CodePlaceHolder(sender, args)
        ) != Unit
        val redeemCode = codeRepo.getCode(args[1])
        val placeHolder = CodePlaceHolder.fetchByDB(plugin, args[1], sender)

        if (redeemCode == null) {
            config.sendMsg(JMessage.Commands.Modify.NOT_FOUND, placeHolder)
            return false
        }
        val console = plugin.server.consoleSender
        val property = args[2]
        when (property) {
            Tab.GeneralActions.Usage.value -> {
                //TODO Remove Usages From Modify and Shift to usage subcommand
                placeHolder.property = property
                config.sendMsg(JMessage.Commands.Modify.LIST, placeHolder)
                return true
            }

            Tab.GeneralActions.Info.value -> {
                val codeInfo = redeemCode.toString()
                sender.sendMessage(codeInfo)
                return true
            }

            Tab.Modify.Enabled.value -> {
                codeRepo.toggleEnabled(redeemCode)
                placeHolder.isEnabled = redeemCode.enabled.toString()
                config.sendMsg(JMessage.Commands.Modify.ENABLED, placeHolder)
                val success = codeRepo.upsertCode(redeemCode)
                if (success) {
                    config.sendMsg(JMessage.Commands.Modify.SUCCESS, placeHolder)
                    return true
                } else {
                    config.sendMsg(JMessage.Commands.Modify.FAILED, placeHolder)
                    return false
                }
            }
            Tab.Modify.SetPermission.value -> {
                if (args.size== 3) {
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
                    val success = codeRepo.upsertCode(redeemCode)
                    if (!success) {
                        config.sendMsg(JMessage.Commands.Modify.FAILED, placeHolder)
                    }
                    return success
                }
            }

            Tab.Modify.Locked.value -> {
                placeHolder.templateLocked = redeemCode.locked.toString()
                if (redeemCode.template.isBlank()) return config.sendMsg(
                    JMessage.Commands.Modify.TEMPLATE_EMPTY,
                    placeHolder
                ) != Unit
                codeRepo.setTemplateLocked(redeemCode, !redeemCode.locked)
                config.sendMsg(JMessage.Commands.Modify.TEMPLATE_LOCKED, placeHolder)
                val success = codeRepo.upsertCode(redeemCode)
                if (success) {
                    config.sendMsg(JMessage.Commands.Modify.SUCCESS, placeHolder)
                    return true
                } else {
                    config.sendMsg(JMessage.Commands.Modify.FAILED, placeHolder)
                    return false
                }
            }

            Tab.Modify.SetTemplate.value -> {
                if (args.size == 3) {
                    redeemCode.template = ""
                    redeemCode.locked = false
                    config.sendMsg(JMessage.Commands.Modify.TEMPLATE_UNSET, placeHolder)
                    val success = codeRepo.upsertCode(redeemCode)
                    if (success) {
                        config.sendMsg(JMessage.Commands.Modify.SUCCESS, placeHolder)
                        return true
                    } else {
                        config.sendMsg(JMessage.Commands.Modify.FAILED, placeHolder)
                        return false
                    }
                }
            }

            Tab.Modify.ListTarget.value -> {
                val targetList = redeemCode.target.joinToString(", ")
                sender.sendMessage(targetList)
                return true
            }

            Tab.Modify.PreviewCommand.value -> {
                redeemCode.commands.values.forEach { plugin.server.dispatchCommand(console, it) }
                return true
            }

            Tab.Modify.SetTarget.value -> {
                if (args.size == 3) {
                    placeHolder.target = ""
                    codeRepo.clearTarget(redeemCode)
                    config.sendMsg(JMessage.Commands.Modify.Target.REMOVE_ALL, placeHolder)
                    val success = codeRepo.upsertCode(redeemCode)
                    if (success) {
                        return true
                    } else {
                        config.sendMsg(JMessage.Commands.Modify.FAILED, placeHolder)
                        return false
                    }
                }
            }
        }

        if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
        placeHolder.property = property
        val value = args[3]
        when (property) {
            Tab.Modify.SetTemplate.value -> {
                val template = config.getTemplate(value) ?: return config.sendMsg(
                    JMessage.Commands.Modify.TEMPLATE_INVALID,
                    placeHolder
                ) != Unit
                placeHolder.template = value
                codeRepo.setTemplate(redeemCode, template)
                codeRepo.setTemplateLocked(redeemCode, true)
                config.sendMsg(JMessage.Commands.Modify.TEMPLATE_SET, placeHolder)
            }

            Tab.Modify.SetDuration.value -> {
                if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                if (!codeRepo.setDuration(
                        redeemCode,
                        value
                    )
                ) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                placeHolder.duration = redeemCode.duration
                config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)
            }

            Tab.Modify.AddDuration.value -> {
                if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                if (!codeRepo.addDuration(
                        redeemCode,
                        value
                    )
                ) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                placeHolder.duration = redeemCode.duration
                config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)
            }

            Tab.Modify.RemoveDuration.value -> {
                if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                if (!codeRepo.removeDuration(
                        redeemCode,
                        value
                    )
                ) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                placeHolder.duration = redeemCode.duration
                config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)
            }

            Tab.Modify.Cooldown.value -> {
                placeHolder.cooldown = value
                if (!codeRepo.setCooldown(
                        redeemCode,
                        value
                    )
                ) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setlastRedeemedTime(redeemCode)
                codeRepo.setCooldown(redeemCode, value)
                config.sendMsg(JMessage.Commands.Modify.COOLDOWN, placeHolder)
            }

            Tab.Modify.SetRedemption.value -> {
                placeHolder.redemption = value
                if (!codeRepo.setMaxRedeems(
                        redeemCode,
                        value.toIntOrNull() ?: 1
                    )
                ) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setMaxRedeems(redeemCode, value.toIntOrNull() ?: 1)
                config.sendMsg(JMessage.Commands.Modify.MAX_REDEEMS, placeHolder)
            }

            Tab.Modify.SetPlayerLimit.value -> {
                placeHolder.playerLimit = value
                if (!codeRepo.setMaxPlayers(
                        redeemCode,
                        value.toIntOrNull() ?: 1
                    )
                ) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setMaxPlayers(redeemCode, value.toIntOrNull() ?: 1)
                config.sendMsg(JMessage.Commands.Modify.MAX_PLAYERS, placeHolder)
            }

            Tab.Modify.SetPermission.value -> {
                placeHolder.permission = value
                if (!codeRepo.setPermission(redeemCode, value)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setPermission(redeemCode, value)
                config.sendMsg(JMessage.Commands.Modify.SET_PERMISSION, placeHolder)
            }

            Tab.Modify.SetPin.value -> {
                placeHolder.pin = value
                if (!codeRepo.setPin(
                        redeemCode,
                        value.toIntOrNull() ?: 0
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
                    JMessage.Commands.Modify.INVALID_COMMAND,
                    placeHolder
                ) != Unit
                codeRepo.setCommands(redeemCode, service.parseToMapId(service.parseToId(commands)))
                sender.sendMessage(placeHolder.command)
                config.sendMsg(JMessage.Commands.Modify.SUCCESS, placeHolder)
            }

            Tab.Modify.AddCommand.value -> {
                val command = args.drop(3).joinToString(" ")
                placeHolder.command = command
                if (command.isBlank()) return config.sendMsg(
                    JMessage.Commands.Modify.INVALID_COMMAND,
                    placeHolder
                ) != Unit
                codeRepo.addCommand(redeemCode, command)
                config.sendMsg(JMessage.Commands.Modify.SUCCESS, placeHolder)
            }

            Tab.Modify.RemoveCommand.value -> {
                placeHolder.commandId = value
                val id = value.toIntOrNull() ?: return config.sendMsg(
                    JMessage.Commands.Modify.INVALID_ID,
                    placeHolder
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
