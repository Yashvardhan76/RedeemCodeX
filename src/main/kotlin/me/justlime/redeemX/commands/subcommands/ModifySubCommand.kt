package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.yml.JMessage
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.command.CommandSender

class ModifySubCommand(private val plugin: RedeemX) : JSubCommand {
    private val service: RedeemCodeService = plugin.service
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        if (args.size < 3) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, CodePlaceHolder(sender, args)) != Unit
        val redeemCode = codeRepo.getCode(args[1])
        val placeHolder = CodePlaceHolder.fetchByDB(plugin, args[1], sender)

        if (redeemCode == null) {
            config.sendMsg(JMessage.Commands.Modify.NOT_FOUND, placeHolder)
            return false
        }
        val property = args[2].lowercase()
        when (property) {
            "list" -> {
                //TODO Remove Usages From Modify and Shift to usage subcommand
                placeHolder.property = property
                config.sendMsg(JMessage.Commands.Modify.LIST, placeHolder)
                return true
            }

            "info" -> {
                val codeInfo = redeemCode.toString()
                sender.sendMessage(codeInfo)
                return true
            }

            "enabled" -> {
                codeRepo.toggleEnabled(redeemCode)
                placeHolder.isEnabled = redeemCode.isEnabled.toString()
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

            "template_locked" -> {
                placeHolder.templateLocked = redeemCode.templateLocked.toString()
                if(redeemCode.template.isBlank()) return config.sendMsg(JMessage.Commands.Modify.TEMPLATE_EMPTY,placeHolder) != Unit
                codeRepo.setTemplateLocked(redeemCode, !redeemCode.templateLocked)
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
        }

        if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
        placeHolder.property = property
        val value = args[3]
        when (property) {
            "command" -> handleCommandModification(sender, args, redeemCode, placeHolder)

            "duration" -> handleDurationModification(args, redeemCode, placeHolder)

            "max_redeems" -> {
                placeHolder.maxRedeems = value
                if (!codeRepo.setMaxRedeems(redeemCode, value.toIntOrNull() ?: 1)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setMaxRedeems(redeemCode, value.toIntOrNull() ?: 1)
                config.sendMsg(JMessage.Commands.Modify.MAX_REDEEMS, placeHolder)
            }

            "max_player" -> {
                placeHolder.maxPlayers = value
                if (!codeRepo.setMaxPlayers(redeemCode, value.toIntOrNull() ?: 1)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setMaxPlayers(redeemCode, value.toIntOrNull() ?: 1)
                config.sendMsg(JMessage.Commands.Modify.MAX_PLAYERS, placeHolder)
            }

            "permission" -> {
                placeHolder.permission = value
                if (!codeRepo.setPermission(redeemCode, value)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setPermission(redeemCode, value)
                config.sendMsg(JMessage.Commands.Modify.PERMISSION, placeHolder)
            }

            "set_pin" -> {
                placeHolder.pin = value
                if (!codeRepo.setPin(redeemCode, value.toIntOrNull() ?: 0)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setPin(redeemCode, value.toIntOrNull() ?: 0)
                config.sendMsg(JMessage.Commands.Modify.PIN, placeHolder)
            }

            "target" -> if (!handleTargetModification(sender, args, redeemCode, placeHolder)) return false

            "cooldown" ->{
                placeHolder.cooldown = value
                if (!codeRepo.setCooldown(redeemCode, value)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setStoredCooldown(redeemCode)
                codeRepo.setCooldown(redeemCode, value)
                config.sendMsg(JMessage.Commands.Modify.COOLDOWN, placeHolder)
            }

            "template" -> {
                placeHolder.template = value
                if (!codeRepo.setTemplate(redeemCode, value)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                codeRepo.setTemplate(redeemCode, value)
                //TODO implement template system
                config.getTemplate()
                codeRepo.setTemplateLocked(redeemCode,true)
                config.sendMsg(JMessage.Commands.Modify.TEMPLATE, placeHolder)
            }

            else -> {
                config.sendMsg(JMessage.Commands.Modify.UNKNOWN_PROPERTY, placeHolder)
                return false
            }

        }
        // Save updated redeem code
        val success = codeRepo.upsertCode(redeemCode)
        if (success) {
            config.sendMsg(JMessage.Commands.Modify.SUCCESS, placeHolder)
            return true
        } else {
            config.sendMsg(JMessage.Commands.Modify.FAILED, placeHolder)
            return false
        }
    }

    private fun handleDurationModification(args: MutableList<String>, redeemCode: RedeemCode, placeHolder: CodePlaceHolder) {
        when (args[3].lowercase()) {
            "set" -> {
                if (args.size < 5) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder)
                val duration = args[4]
                placeHolder.duration = duration
                if (!codeRepo.setDuration(redeemCode, duration)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder)
                config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)
            }

            "add" -> {
                if (args.size < 5) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder)
                val duration = args[4]
                placeHolder.duration = duration
                if (!codeRepo.addDuration(redeemCode, duration)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder)
                config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)

            }

            "remove" -> {
                if (args.size < 5) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder)
                val duration = args[4]
                placeHolder.duration = duration
                if (!codeRepo.removeDuration(redeemCode, duration)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder)
                config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)

            }

            "clear" -> {
                if (!codeRepo.clearDuration(redeemCode)) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder)
                config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)
            }

            else -> config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder)
        }
    }

    private fun handleCommandModification(sender: CommandSender, args: MutableList<String>, redeemCode: RedeemCode, placeHolder: CodePlaceHolder) {
        val method = args[3].lowercase()
        val console = plugin.server.consoleSender

        when (method) {
            "add" -> {
                val command = args.drop(4).joinToString(" ")
                placeHolder.command = command
                if (command.isBlank()) return config.sendMsg(JMessage.Commands.Modify.INVALID_COMMAND, placeHolder)
                codeRepo.addCommand(redeemCode, command)
            }

            "remove" -> {
                placeHolder.commandId = args[4]
                val id = args.getOrNull(4)?.toIntOrNull() ?: return config.sendMsg(JMessage.Commands.Modify.INVALID_ID, placeHolder)
                codeRepo.removeCommand(redeemCode, id)
            }

            //TODO LIST Must be Removed
            "list" -> {
                val commandsList = redeemCode.commands.values.joinToString("\n")
                sender.sendMessage(commandsList)
                config.sendMsg(JMessage.Commands.Modify.LIST, placeHolder)
                return
            }

            "set" -> {
                val commands = args.drop(4).joinToString(" ")
                placeHolder.command = commands
                if (commands.isBlank()) return config.sendMsg(JMessage.Commands.Modify.INVALID_COMMAND, placeHolder)
                codeRepo.setCommands(redeemCode, service.parseToMapId(commands))
            }

            "clear" -> {
                codeRepo.clearCommands(redeemCode)
            }

            "preview" -> redeemCode.commands.values.forEach { plugin.server.dispatchCommand(console, it) }

            else -> {
                config.sendMsg(JMessage.Commands.Modify.UNKNOWN_METHOD, placeHolder)
            }
        }
    }

    private fun handleTargetModification(sender: CommandSender, args: MutableList<String>, redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        when (args[3]) {
            "add" -> {
                if (args.size < 5) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                placeHolder.target = args[4]
                codeRepo.addTarget(redeemCode, args[4])
                config.sendMsg(JMessage.Commands.Modify.Target.ADD, placeHolder)
            }

            "set" -> {
                if (args.size < 5) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                placeHolder.target = args[4]
                codeRepo.setTarget(redeemCode, args.drop(4))
                config.sendMsg(JMessage.Commands.Modify.Target.SET, placeHolder)
            }

            "remove" -> {
                if (args.size < 5) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                placeHolder.target = args[4]
                codeRepo.removeTarget(redeemCode, args[4])
                config.sendMsg(JMessage.Commands.Modify.Target.REMOVE, placeHolder)
            }

            "clear" -> {
                redeemCode.target = mutableListOf()
            }

            //TODO Remove Usages From Modify and Shift to usage subcommand
            "list" -> {
                sender.sendMessage(redeemCode.target.joinToString("\n"))
            }

            else -> {
                config.sendMsg(JMessage.Commands.Modify.Target.UNKNOWN_METHOD, placeHolder)
                return false
            }
        }
        return true
    }

}
