package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.state.RedeemCodeState
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.command.CommandSender

class ModifySubCommand(private val plugin: RedeemX) {
    private val config: ConfigManager = ConfigManager(plugin)
    private val stateManager = plugin.stateManager
    private val service = RedeemCodeService(plugin)

    fun execute(sender: CommandSender, args: Array<out String>) {

        val state = stateManager.createState(sender)

        if (args.size < 3) return config.sendMessage("commands.modify.invalid-syntax", state)

        state.inputCode = args[1]
        state.inputTemplate = args[1]
        if (!stateManager.fetchState(sender, state.inputCode)) {
        } else if (state.templateName == state.inputTemplate) {
        } else return config.sendMessage(
            "commands.modify" + ".not-found", state
        )

        state.property = args[2].lowercase()
        when (state.property) {
            "list" -> {
                val codeInfo = plugin.redeemCodeDB.get(state.inputCode)?.toString() ?: "Code not found."
                sender.sendMessage(codeInfo)
                return
            }

            "enabled" -> {
                state.isEnabled = !state.isEnabled
                return
            }
        }

        if (args.size < 4) return config.sendMessage("commands.modify.invalid-syntax", state)


        state.value = args[3]
        when (state.property) {
            "command" -> handleCommandModification(args, state)

            "duration" -> service.handleDurationModification(state.value, args.getOrNull(4), state, config)

            "max_redeems" -> state.maxRedeems =
                state.value.toIntOrNull() ?: return config.sendMessage("commands.modify.invalid-value", state)

            "max_player" -> state.maxPlayers =
                state.value.toIntOrNull() ?: return config.sendMessage("commands.modify.invalid-value", state)

            "permission" -> state.permission =
                if (state.value.equals("true", ignoreCase = true)) config.getString("modify.permission")
                    ?.replace("{code}", state.inputCode)
                else if (!state.value.equals("false", ignoreCase = true)) state.value else null

            "set_pin" -> state.pin =
                state.value.toIntOrNull() ?: return config.sendMessage("commands.modify.invalid-value", state)

            "target" -> if (!handleTargetModification(args, state)) return

            else -> return config.sendMessage("commands.modify.unknown-property", state)

        }

        // Save updated redeem code
        val success = stateManager.updateDb(sender)
        if (success) {
            config.sendMessage("commands.modify.success", state)
        } else {
            config.sendMessage("commands.modify.failed", state)
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
                    config.sendMessage("commands.modify.command.no-command", state)
                    return
                }
                val id = list.keys.maxOrNull() ?: 0
                list[id + 1] = commandValue
            }

            "remove" -> {
                val id = args.getOrNull(4)?.toIntOrNull()
                if (id == null) {
                    config.sendMessage("commands.modify.command.invalid-id", state)
                    return
                }
                list.remove(id)
            }

            "list" -> {
                val commandsList = list.values.joinToString("\n")
                state.sender.sendMessage(commandsList)
                config.sendMessage("commands.modify.command.list", state)
                return
            }

            "set" -> {
                val id = args.getOrNull(4)?.toIntOrNull()
                val commandValue = args.drop(5).joinToString(" ")
                if (id == null || commandValue.isBlank()) {
                    config.sendMessage("commands.modify.command.invalid-set", state)
                    return
                }
                list[id] = commandValue
            }

            "preview" -> list.values.forEach { plugin.server.dispatchCommand(console, it) }

            else -> {
                config.sendMessage("commands.modify.command.unknown-method", state)
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
                config.sendMessage("commands.modify.target.add", state)
            }

            "set" -> {
                state.target = mutableListOf(args.getOrNull(4))
                config.sendMessage("commands.modify.target.set", state)
            }

            "remove" -> {
                state.target.remove(args.getOrNull(4))

                config.sendMessage("commands.modify.target.remove", state)
            }

            "remove_all" -> {
                state.target = mutableListOf()
            }

            "list" -> {
                state.sender.sendMessage(state.target.joinToString("\n"))
            }

            else -> {
                config.sendMessage("commands.modify.target.unknown-method", state)
                return false
            }
        }
        return true
    }

}
