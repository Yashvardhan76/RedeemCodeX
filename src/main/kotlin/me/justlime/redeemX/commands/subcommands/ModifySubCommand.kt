package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.data.service.RedeemCodeService
import me.justlime.redeemX.state.RedeemCodeState
import org.bukkit.command.CommandSender
import java.time.LocalDateTime
import java.time.ZoneId

class ModifySubCommand(private val plugin: RedeemX) {
    private val config: ConfigManager = ConfigManager(plugin)
    private val stateManager = plugin.stateManager

    fun execute(sender: CommandSender, args: Array<out String>) {
        val state = stateManager.createState(sender)

        // Validate arguments
        if (args.size < 3) {
            config.sendMessage("commands.modify.invalid-syntax", state)
            return
        }

        state.inputCode = args[1]
        state.property = args[2].lowercase()

        if (state.property == "list") {
            val codeInfo = plugin.redeemCodeDB.get(state.inputCode)?.toString() ?: "Code not found."
            sender.sendMessage(codeInfo)
            return
        }

        if (args.size < 4) {
            config.sendMessage("commands.modify.invalid-syntax", state)
            return
        }

        state.value = args[3]

        // Fetch the redeem code
        if (!stateManager.fetchState(sender, state.inputCode)) {
            config.sendMessage("commands.modify.not-found", state)
            return
        }

        when (state.property) {
            "max_redeems" -> state.maxRedeems =
                state.value.toIntOrNull() ?: return config.sendMessage("commands.modify.invalid-value", state)

            "max_player" -> state.maxPlayers =
                state.value.toIntOrNull() ?: return config.sendMessage("commands.modify.invalid-value", state)

            "duration" -> handleDurationModification(state.value, args.getOrNull(4), state)

            "permission" -> state.permission =
                if (state.value.equals("true", ignoreCase = true)) config.getString("modify.permission")
                    ?.replace("{code}", state.inputCode) else if (!state.value.equals(
                        "false", ignoreCase = true
                    )
                ) state.value else null

            "set_target" -> handleTargetModification(args, state)

            "set_pin" -> state.pin =
                state.value.toIntOrNull() ?: return config.sendMessage("commands.modify.invalid-value", state)

            "enabled" -> state.isEnabled = state.value.lowercase() == "true"

            "command" -> handleCommandModification(args, state)

            else -> {
                config.sendMessage("commands.modify.unknown-property", state)
                return
            }
        }

        // Save updated redeem code
        val success = stateManager.updateDb(sender)
        if (success) {
            config.sendMessage("commands.modify.success", state)
        } else {
            config.sendMessage("commands.modify.failed", state)
        }
    }

    private fun handleDurationModification(action: String, adjustmentDuration: String?, state: RedeemCodeState) {
        val service = RedeemCodeService(plugin)
        val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
        val currentTime: LocalDateTime = LocalDateTime.now(timeZoneId)
        if (state.storedTime == null) state.storedTime = currentTime

        val existingDuration = state.duration ?: "0s"
        val durationValue = adjustmentDuration ?: "0"

        when (action.lowercase()) {
            "set" -> {
                state.storedTime = currentTime
                state.duration = service.adjustDuration("0s", durationValue, isAdding = true).toString() + 's'
            }

            "add" -> state.duration =
                service.adjustDuration(existingDuration, durationValue, isAdding = true).toString() + 's'

            "remove" -> {
                val duration =
                    service.adjustDuration(existingDuration, durationValue, isAdding = false).toString() + 's'
                state.duration = if ((duration.dropLast(1).toIntOrNull() ?: -1) < 0) null else duration
            }

            else -> {
                config.sendMessage(
                    "commands.modify.duration-invalid", state = state
                )
            }
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

    private fun handleTargetModification(args: Array<out String>, state: RedeemCodeState) {
        val tempList: MutableList<String?> = mutableListOf()
         state.target.forEach{
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

            "remove" ->{
                state.target.remove(args.getOrNull(4))

                config.sendMessage("commands.modify.target.remove", state)
            }

            "remove_all" ->{
                state.target = mutableListOf()
            }

            else -> {
                config.sendMessage("commands.modify.target.unknown-method", state)
            }
        }
    }

}
