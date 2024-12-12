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
    private val db = plugin.redeemCodeDB

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        if (args.size < 3) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, CodePlaceHolder(sender,args)) != Unit
        val redeemCode = codeRepo.getCode(args[1])
        val placeHolder = CodePlaceHolder.fetchByDB(plugin,args[1],sender)

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
                redeemCode.isEnabled = !redeemCode.isEnabled
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
        }

        if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
        val value = args[3]
        when (property) {
            "command" -> handleCommandModification(sender, args, redeemCode, placeHolder)

            "duration" -> handleDurationModification(sender, args, redeemCode,placeHolder)

            "max_redeems" -> {
                placeHolder.maxRedeemsPerPlayer = value
                redeemCode.maxRedeems = value.toIntOrNull() ?: 0
                if (redeemCode.maxRedeems < 1) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                config.sendMsg(JMessage.Commands.Modify.MAX_REDEEMS, placeHolder)
            }

            "max_player" -> {
                placeHolder.maxPlayersCanRedeem= value
                redeemCode.maxPlayers = value.toIntOrNull() ?: 0
                if (redeemCode.maxPlayers < 1) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                config.sendMsg(JMessage.Commands.Modify.MAX_PLAYERS, placeHolder)
            }

            "permission" -> {
                placeHolder.permission = value
                redeemCode.permission = if (value.equals("true", ignoreCase = true)) config.getConfigValue("modify.permission").replace("{code}", redeemCode.code)
                else if (!value.equals("false", ignoreCase = true)) value else null
                config.sendMsg(JMessage.Commands.Modify.PERMISSION, placeHolder)
            }

            "set_pin" -> {
                placeHolder.pin = value
                redeemCode.pin = value.toIntOrNull() ?: 0
                if (redeemCode.pin < 1) return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, placeHolder) != Unit
                config.sendMsg(JMessage.Commands.Modify.PIN, placeHolder)
            }

            "target" -> if (!handleTargetModification(sender,args,redeemCode,placeHolder)) return false

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

    private fun handleDurationModification(sender: CommandSender, args: MutableList<String>, redeemCode: RedeemCode,placeHolder: CodePlaceHolder) {
        if (redeemCode.storedTime == null) redeemCode.storedTime = service.currentTime
        val existingDuration = redeemCode.duration ?: "0s"
        val action = args[3].lowercase()
        val durationValue = args[4]


        when (action.lowercase()) {
            "set" -> {
                redeemCode.storedTime = service.currentTime
                redeemCode.duration = service.adjustDuration("0s", durationValue, isAdding = true).toString() + 's'
                placeHolder.duration = durationValue
                config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)

            }

            "add" -> {
                redeemCode.duration = service.adjustDuration(existingDuration, durationValue, isAdding = true).toString() + 's'
                placeHolder.duration = durationValue
                config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)

            }

            "remove" -> {
                val duration = service.adjustDuration(existingDuration, durationValue, isAdding = false).toString() + 's'
                redeemCode.duration = if ((duration.dropLast(1).toIntOrNull() ?: -1) < 0) null else duration
                placeHolder.duration = durationValue
                config.sendMsg(JMessage.Commands.Modify.DURATION, placeHolder)

            }

            else -> config.sendMsg("commands.modify.duration-invalid", placeHolder)

        }
    }

    private fun handleCommandModification(sender: CommandSender, args: MutableList<String>, redeemCode: RedeemCode, placeHolder: CodePlaceHolder) {
        val method = args[3].lowercase()
        val list = redeemCode.commands
        val console = plugin.server.consoleSender

        when (method) {
            "add" -> {
                val command = args.drop(4).joinToString(" ")
                placeHolder.command = command
                if (command.isBlank()) {
                    config.sendMsg(JMessage.Commands.Modify.INVALID_COMMAND, placeHolder)
                    return
                }
                val id = list.keys.maxOrNull() ?: 0
                list[id + 1] = command
            }

            "remove" -> {
                placeHolder.commandId = args[4]
                val id = args.getOrNull(4)?.toIntOrNull()
                if (id == null) {
                    config.sendMsg(JMessage.Commands.Modify.INVALID_ID, placeHolder)
                    return
                }
                list.remove(id)
            }

            //TODO LIST Must be Removed
            "list" -> {
                val commandsList = list.values.joinToString("\n")
                sender.sendMessage(commandsList)
                config.sendMsg(JMessage.Commands.Modify.LIST, placeHolder)
                return
            }

            "set" -> {
                placeHolder.commandId = args[4]
                val id = args.getOrNull(4)?.toIntOrNull()
                val commandValue = args.drop(5).joinToString(" ")
                if (id == null || commandValue.isBlank()) {
                    config.sendMsg(JMessage.Commands.Modify.INVALID_SET, placeHolder)
                    return
                }
                list[id] = commandValue
            }

            "preview" -> list.values.forEach { plugin.server.dispatchCommand(console, it) }

            else -> {
                config.sendMsg(JMessage.Commands.Modify.UNKNOWN_METHOD, placeHolder)
            }
        }
    }

    private fun handleTargetModification(sender: CommandSender, args: MutableList<String>,redeemCode: RedeemCode, placeHolder: CodePlaceHolder): Boolean {
        val tempList: MutableList<String?> = mutableListOf()
        redeemCode.target.forEach {
            tempList.add(it?.trim())
        }
        redeemCode.target = tempList.distinct().toMutableList()
        when (args[3]) {
            "add" -> {
                if (args.size < 5) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                placeHolder.target = args[4]
                redeemCode.target.add(args.getOrNull(4))
                redeemCode.target = redeemCode.target.distinct().toMutableList()
                config.sendMsg(JMessage.Commands.Modify.Target.ADD, placeHolder)
            }

            "set" -> {
                if (args.size < 5) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                placeHolder.target = args[4]
                redeemCode.target = mutableListOf(args.getOrNull(4))
                config.sendMsg(JMessage.Commands.Modify.Target.SET, placeHolder)
            }

            "remove" -> {
                if (args.size < 5) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, placeHolder) != Unit
                placeHolder.target = args[4]
                redeemCode.target.remove(args.getOrNull(4))
                config.sendMsg(JMessage.Commands.Modify.Target.REMOVE, placeHolder)
            }

            "remove_all" -> {
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
