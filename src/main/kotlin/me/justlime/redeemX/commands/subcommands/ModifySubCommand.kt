package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.config.JMessage
import me.justlime.redeemX.state.RedeemCodeState
import me.justlime.redeemX.state.StateManager
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.command.CommandSender

class ModifySubCommand(private val plugin: RedeemX) : JSubCommand {
    private val config: ConfigManager = plugin.configFile
    private val stateManager: StateManager = plugin.stateManager
    private val service: RedeemCodeService = plugin.service
    private val db = plugin.redeemCodeDB

    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {

        val state = stateManager.createState(sender)

        if (args.size < 3) return config.dm(JMessage.Commands.Modify.INVALID_SYNTAX, state) != Unit

        state.inputCode = args[1]
        state.inputTemplate = args[1]
        if (!stateManager.fetchState(sender, state.inputCode)) {
            config.dm(JMessage.Commands.Modify.NOT_FOUND, state)
            return false
        }

        state.property = args[2].lowercase()
        when (state.property) {
            "list" -> {
                sender.sendMessage(state.toString())
                return true
            }

            "info" -> {
                val codeInfo = db.get(state.inputCode)?.toString() ?: return config.dm(JMessage.Commands.Modify.NOT_FOUND, state) != Unit
                sender.sendMessage(codeInfo)
                return true
            }

            "enabled" -> {
                state.isEnabled = !state.isEnabled
                return true
            }
        }

        if (args.size < 4) return config.dm(JMessage.Commands.Modify.INVALID_SYNTAX, state) != Unit


        state.value = args[3]
        when (state.property) {
            "command" -> handleCommandModification(args, state)

            "duration" -> service.handleDurationModification(state.value, args.getOrNull(4), state, config)

            "max_redeems" -> state.maxRedeems = state.value.toIntOrNull() ?: return config.dm(JMessage.Commands.Modify.INVALID_VALUE, state) != Unit

            "max_player" -> state.maxPlayers = state.value.toIntOrNull() ?: return config.dm(JMessage.Commands.Modify.INVALID_VALUE, state) != Unit

            "permission" -> state.permission = if (state.value.equals("true", ignoreCase = true)) config.getString("modify.permission")?.replace("{code}", state.inputCode)
            else if (!state.value.equals("false", ignoreCase = true)) state.value else null

            "set_pin" -> {
                state.pin = state.value.toIntOrNull() ?: return config.dm(JMessage.Commands.Modify.INVALID_VALUE, state) != Unit
                config.dm(JMessage.Commands.Modify.SET_PIN, state)
                return true
            }

            "target" -> if (!handleTargetModification(args, state)) return false

            else -> {
                config.dm(JMessage.Commands.Modify.UNKNOWN_PROPERTY, state)
                return false
            }

        }

        // Save updated redeem code
        val success = stateManager.updateDb(sender)
        if (success) {
            config.dm(JMessage.Commands.Modify.SUCCESS, state)
            return true
        } else {
            config.dm(JMessage.Commands.Modify.FAILED, state)
            return false
        }
    }

    private fun handleCommandModification(args: Array<out String>, state: RedeemCodeState) {
        val method = args[3].lowercase()
        val list = state.commands
        val console = plugin.server.consoleSender

        when (method) {
            "add" -> {
                val commandValue = args.drop(4).joinToString(" ")
                if (commandValue.isBlank()) {
                    config.dm(JMessage.Commands.Modify.INVALID_COMMAND, state)
                    return
                }
                val id = list.keys.maxOrNull() ?: 0
                list[id + 1] = commandValue
            }

            "remove" -> {
                val id = args.getOrNull(4)?.toIntOrNull()
                if (id == null) {
                    config.dm(JMessage.Commands.Modify.INVALID_ID, state)
                    return
                }
                list.remove(id)
            }

            "list" -> {
                val commandsList = list.values.joinToString("\n")
                state.sender.sendMessage(commandsList)
                config.dm(JMessage.Commands.Modify.LIST, state)
                return
            }

            "set" -> {
                val id = args.getOrNull(4)?.toIntOrNull()
                val commandValue = args.drop(5).joinToString(" ")
                if (id == null || commandValue.isBlank()) {
                    config.dm(JMessage.Commands.Modify.INVALID_SET, state)
                    return
                }
                list[id] = commandValue
            }

            "preview" -> list.values.forEach { plugin.server.dispatchCommand(console, it) }

            else -> {
                config.dm(JMessage.Commands.Modify.UNKNOWN_METHOD, state)
            }
        }
    }

    private fun handleTargetModification(args: Array<out String>, state: RedeemCodeState): Boolean {
        val tempList: MutableList<String?> = mutableListOf()
        state.target.forEach {
            tempList.add(it?.trim())
        }
        state.target = tempList.distinct().toMutableList()
        when (args[3]) {
            "add" -> {
                state.target.add(args.getOrNull(4))
                state.target = state.target.distinct().toMutableList()
                config.dm(JMessage.Commands.Modify.Target.ADD, state)
            }

            "set" -> {
                state.target = mutableListOf(args.getOrNull(4))
                config.dm(JMessage.Commands.Modify.Target.SET, state)
            }

            "remove" -> {
                state.target.remove(args.getOrNull(4))

                config.dm(JMessage.Commands.Modify.Target.REMOVE, state)
            }

            "remove_all" -> {
                state.target = mutableListOf()
            }

            "list" -> {
                state.sender.sendMessage(state.target.joinToString("\n"))
            }

            else -> {
                config.dm(JMessage.Commands.Modify.Target.UNKNOWN_METHOD, state)
                return false
            }
        }
        return true
    }

}
