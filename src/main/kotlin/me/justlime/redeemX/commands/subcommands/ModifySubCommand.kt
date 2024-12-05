package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.ConfigManager
import me.justlime.redeemX.data.config.raw.JMessage
import me.justlime.redeemX.state.RedeemCodeState
import me.justlime.redeemX.state.StateManager
import me.justlime.redeemX.utilities.RedeemCodeService

class ModifySubCommand(private val plugin: RedeemX) : JSubCommand {
    private val config: ConfigManager = plugin.configFile
    private val stateManager: StateManager = plugin.stateManager
    private val service: RedeemCodeService = plugin.service
    private val db = plugin.redeemCodeDB

    override fun execute(state: RedeemCodeState): Boolean{
        val sender = state.sender
        val args = state.args
        if (args.size < 3) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, state) != Unit


        state.inputCode = args[1]
        state.inputTemplate = args[1]
        if (!stateManager.fetchState(state)) {
            config.sendMsg(JMessage.Commands.Modify.NOT_FOUND, state)
            return false
        }
        state.template = state.inputTemplate
        state.property = args[2].lowercase()
        when (state.property) {
            "list" -> {
                config.sendMsg(JMessage.Commands.Modify.LIST, state)
                return true
            }

            "info" -> {
                val codeInfo = db.get(state.code)?.toString() ?: return config.sendMsg(JMessage.Commands.Modify.NOT_FOUND, state) != Unit
                sender.sendMessage(codeInfo)
                return true
            }

            "enabled" -> {
                state.isEnabled = !state.isEnabled
                return true
            }
        }

        if (args.size < 4) return config.sendMsg(JMessage.Commands.Modify.INVALID_SYNTAX, state) != Unit


        state.value = args[3]
        when (state.property) {
            "command" -> handleCommandModification(args, state)

            "duration" -> service.handleDurationModification(state.value, args.getOrNull(4), state, config)

            "max_redeems" -> state.maxRedeems = state.value.toIntOrNull() ?: return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, state) != Unit

            "max_player" -> state.maxPlayers = state.value.toIntOrNull() ?: return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, state) != Unit

            "permission" -> state.permission = if (state.value.equals("true", ignoreCase = true)) config.getString("modify.permission")?.replace("{code}", state.code)
            else if (!state.value.equals("false", ignoreCase = true)) state.value else null

            "pin" -> {
                state.pin = state.value.toIntOrNull() ?: return config.sendMsg(JMessage.Commands.Modify.INVALID_VALUE, state) != Unit
                config.sendMsg(JMessage.Commands.Modify.PIN, state)
            }

            "target" -> if (!handleTargetModification(args, state)) return false

            else -> {
                config.sendMsg(JMessage.Commands.Modify.UNKNOWN_PROPERTY, state)
                return false
            }

        }
        state.sender.sendMessage(state.pin.toString())

        stateManager.updateState(state)


        // Save updated redeem code
        val success = stateManager.updateDb(sender)
        if (success) {
            config.sendMsg(JMessage.Commands.Modify.SUCCESS, state)
            return true
        } else {
            config.sendMsg(JMessage.Commands.Modify.FAILED, state)
            return false
        }
    }

    private fun handleCommandModification(args: MutableList<String>, state: RedeemCodeState) {
        val method = args[3].lowercase()
        val list = state.commands
        val console = plugin.server.consoleSender

        when (method) {
            "add" -> {
                val commandValue = args.drop(4).joinToString(" ")
                if (commandValue.isBlank()) {
                    config.sendMsg(JMessage.Commands.Modify.INVALID_COMMAND, state)
                    return
                }
                val id = list.keys.maxOrNull() ?: 0
                list[id + 1] = commandValue
            }

            "remove" -> {
                val id = args.getOrNull(4)?.toIntOrNull()
                if (id == null) {
                    config.sendMsg(JMessage.Commands.Modify.INVALID_ID, state)
                    return
                }
                list.remove(id)
            }

            "list" -> {
                val commandsList = list.values.joinToString("\n")
                state.sender.sendMessage(commandsList)
                config.sendMsg(JMessage.Commands.Modify.LIST, state)
                return
            }

            "set" -> {
                val id = args.getOrNull(4)?.toIntOrNull()
                val commandValue = args.drop(5).joinToString(" ")
                if (id == null || commandValue.isBlank()) {
                    config.sendMsg(JMessage.Commands.Modify.INVALID_SET, state)
                    return
                }
                list[id] = commandValue
            }

            "preview" -> list.values.forEach { plugin.server.dispatchCommand(console, it) }

            else -> {
                config.sendMsg(JMessage.Commands.Modify.UNKNOWN_METHOD, state)
            }
        }
    }

    private fun handleTargetModification(args: MutableList<String>, state: RedeemCodeState): Boolean {
        val tempList: MutableList<String?> = mutableListOf()
        state.target.forEach {
            tempList.add(it?.trim())
        }
        state.target = tempList.distinct().toMutableList()
        when (args[3]) {
            "add" -> {
                state.target.add(args.getOrNull(4))
                state.target = state.target.distinct().toMutableList()
                config.sendMsg(JMessage.Commands.Modify.Target.ADD, state)
            }

            "set" -> {
                state.target = mutableListOf(args.getOrNull(4))
                config.sendMsg(JMessage.Commands.Modify.Target.SET, state)
            }

            "remove" -> {
                state.target.remove(args.getOrNull(4))

                config.sendMsg(JMessage.Commands.Modify.Target.REMOVE, state)
            }

            "remove_all" -> {
                state.target = mutableListOf()
            }

            "list" -> {
                state.sender.sendMessage(state.target.joinToString("\n"))
            }

            else -> {
                config.sendMsg(JMessage.Commands.Modify.Target.UNKNOWN_METHOD, state)
                return false
            }
        }
        return true
    }

}
