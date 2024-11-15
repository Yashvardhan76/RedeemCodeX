package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.state.StateManager
import me.justlime.redeemX.utilities.StateMap
import org.bukkit.command.CommandSender
import java.time.LocalDateTime
import java.time.ZoneId

class ModifySubCommand(private val plugin: RedeemX, private val stateManager: StateManager) {
    private val config: ConfigManager = ConfigManager(plugin, stateManager = stateManager)

    fun execute(sender: CommandSender, args: Array<out String>) {
        val state = stateManager.getOrCreateState(sender)

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
        val redeemCode = plugin.redeemCodeDB.get(state.inputCode)
        if (redeemCode == null) {
            config.sendMessage("commands.modify.not-found", state)
            return
        }

        when (state.property) {
            "max_redeems" -> redeemCode.maxRedeems =
                state.value.toIntOrNull() ?: return config.sendMessage("commands.modify.invalid-value", state)

            "max_player" -> redeemCode.maxPlayers =
                state.value.toIntOrNull() ?: return config.sendMessage("commands.modify.invalid-value", state)

            "duration" -> handleDurationModification(redeemCode, state.value, args.getOrNull(4))

            "permission" -> redeemCode.permission = state.value

            "set_target" -> redeemCode.target = state.value

            "set_pin" -> redeemCode.pin = state.value.toIntOrNull()
                ?: return config.sendMessage("commands.modify.invalid-value", state)

            "enabled" -> redeemCode.isEnabled = state.value.lowercase() == "true"

            "command" -> handleCommandModification(redeemCode, args)

            else -> {
                config.sendMessage("commands.modify.unknown-property", state)
                return
            }
        }

        // Save updated redeem code
        val success = plugin.redeemCodeDB.upsert(redeemCode)
        if (success) {
            config.sendMessage("commands.modify.success", state)
        } else {
            config.sendMessage("commands.modify.failed", state)
        }
    }

    private fun handleDurationModification(redeemCode: RedeemCode, action: String, adjustmentDuration: String?) {
        val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
        val currentTime: LocalDateTime = LocalDateTime.now(timeZoneId)
        if (redeemCode.storedTime == null) redeemCode.storedTime = currentTime

        val existingDuration = redeemCode.duration ?: "0s"
        val durationValue = adjustmentDuration ?: "0"

        when (action.lowercase()) {
            "set" -> {
                redeemCode.storedTime = currentTime
                redeemCode.duration = adjustDuration("0s", durationValue, isAdding = true).toString() + 's'
            }

            "add" -> redeemCode.duration =
                adjustDuration(existingDuration, durationValue, isAdding = true).toString() + 's'

            "remove" -> {
                val duration = adjustDuration(existingDuration, durationValue, isAdding = false).toString() + 's'
                redeemCode.duration = if ((duration.dropLast(1).toIntOrNull() ?: -1) < 0) null else duration
            }

            else -> {
                config.sendMessage("commands.modify.duration-invalid",StateMap.toState(redeemCode))
            }
        }
    }

    private fun handleCommandModification(redeemCode: RedeemCode, args: Array<out String>) {
        val method = args[3].lowercase()
        val list = redeemCode.commands
        val console = plugin.server.consoleSender

        when (method) {
            "add" -> {
                val commandValue = args.drop(4).joinToString(" ")
                if (commandValue.isBlank()) {
                    config.sendMessage("commands.modify.command.no-command", mapOf())
                    return
                }
                val id = list.keys.maxOrNull() ?: 0
                list[id + 1] = commandValue
            }

            "list" -> {
                val commandsList = list.values.joinToString("\n")
                config.sendMessage("commands.modify.command.list", mapOf("commands" to commandsList))
                return
            }

            "set" -> {
                val id = args.getOrNull(4)?.toIntOrNull()
                val commandValue = args.drop(5).joinToString(" ")
                if (id == null || commandValue.isBlank()) {
                    config.sendMessage("commands.modify.command.invalid-set", mapOf())
                    return
                }
                list[id] = commandValue
            }

            "preview" -> list.values.forEach { plugin.server.dispatchCommand(console, it) }

            else -> {
                config.sendMessage("commands.modify.command.unknown-method", mapOf("method" to method))
            }
        }
    }

    private fun adjustDuration(existingDuration: String, adjustmentDuration: String, isAdding: Boolean): Long {
        val timeUnitToSeconds =
            mapOf("s" to 1L, "m" to 60L, "h" to 3600L, "d" to 86400L, "mo" to 2592000L, "y" to 31536000L)

        val existingAmount = existingDuration.dropLast(1).toLongOrNull() ?: return 0L
        val existingUnit = existingDuration.takeLast(1)
        val adjustmentAmount = adjustmentDuration.dropLast(1).toLongOrNull() ?: return 0L
        val adjustmentUnit = adjustmentDuration.takeLast(1)

        val existingSeconds = existingAmount * (timeUnitToSeconds[existingUnit] ?: return 0L)
        val adjustmentSeconds = adjustmentAmount * (timeUnitToSeconds[adjustmentUnit] ?: return 0L)

        return if (isAdding) existingSeconds + adjustmentSeconds else existingSeconds - adjustmentSeconds
    }
}
