package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import org.bukkit.command.CommandSender
import java.time.LocalDateTime
import java.time.ZoneId

class ModifySubCommand(private val plugin: RedeemX) {
    fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendMessage("Usage: /rxc modify <code> <property>")
            return
        }

        val code = args[1]
        val property = args[2].lowercase()

        if (property == "list") {
            sender.sendMessage(plugin.redeemCodeDB.get(code)?.toString())
            return
        }
        if (args.size < 4) {
            sender.sendMessage("Usage: /rxc modify <code> <property>")
            return
        }
        val value = args[3]

        // Attempt to find the redeem code by the provided code
        val redeemCode = plugin.redeemCodeDB.get(code)
        if (redeemCode == null) {
            sender.sendMessage("The code '$code' does not exist.")
            return
        }

        when (property) {

            "max_redeems" -> redeemCode.maxRedeems =
                value.toIntOrNull() ?: return sender.sendMessage("Invalid value for max_redeems.")

            "max_player" -> redeemCode.maxPlayers =
                value.toIntOrNull() ?: return sender.sendMessage("Invalid value for max_per_player.")

            "duration" -> {
                val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
                val currentTime: LocalDateTime = LocalDateTime.now(timeZoneId)
                if (redeemCode.storedTime == null) redeemCode.storedTime = currentTime

                // Determine the action to perform on duration
                val durationAction = value.lowercase()
                var durationValue = args.getOrNull(4)
                var existingDuration = redeemCode.duration
                if (existingDuration == null) existingDuration = "0s"
                if (durationValue == null) durationValue = "0"

                when (durationAction) {
                    "set" -> {
                        redeemCode.storedTime = currentTime
                        redeemCode.duration = adjustDuration("0s", durationValue, isAdding = true).toString() + 's'
                    }

                    "add" -> redeemCode.duration =
                        adjustDuration(existingDuration, durationValue, isAdding = true).toString() + 's'

                    "remove" -> {
                        val duration =
                            adjustDuration(existingDuration, durationValue, isAdding = false).toString() + 's'
                        redeemCode.duration = if ((duration.drop(1).toIntOrNull() ?: -1) < 0) null else duration

                    }

                    else -> {
                        println("Invalid duration operation specified: $durationAction")
                    }
                }
                val success = plugin.redeemCodeDB.upsert(redeemCode)
                if (success) {
                    sender.sendMessage("Updated $property for code '${redeemCode.code}' to ${redeemCode.duration} ")
                } else {
                    sender.sendMessage("Failed to updated the code: ${redeemCode.code}")
                    return
                }
                return
            }

            "permission" -> redeemCode.permission = value

            "set_target" -> redeemCode.target = value

            "set_pin" -> redeemCode.pin = value.toIntOrNull() ?: return sender.sendMessage("Invalid value for set_pin.")

            "enabled" -> redeemCode.isEnabled = value.lowercase() == "true"

            "command" -> {

                val method = args[3].lowercase()
                val list = redeemCode.commands
                val console = plugin.server.consoleSender
                when (method) {
                    "add" -> {
                        val commandValue = args.drop(4).joinToString(" ")
                        if (args.size <= 4 || commandValue.isBlank()) {
                            sender.sendMessage("No command Passed")
                            return
                        }
                        val id = redeemCode.commands.keys.maxOrNull() ?: 0
                        redeemCode.commands[id + 1] = commandValue
                    }

                    "list" -> {
                        sender.sendMessage(list.values.joinToString("\n"))
                        return
                    }

                    "set" -> {

                        val commandValue = args.drop(5).joinToString(" ")
                        val id = args[4].toIntOrNull()
                        if (args.size <= 5 || commandValue.isBlank()) {
                            sender.sendMessage("No command Passed")
                            return
                        }
                        if (id == null) {
                            sender.sendMessage("ID Not Passed")
                            return
                        }

                        redeemCode.commands[id] = commandValue
                    }

                    "preview" -> {
                        if (!list.values.isEmpty()) list.values.forEach { plugin.server.dispatchCommand(console, it) }
                        return
                    }

                    else -> {
                        sender.sendMessage("Unknown method '$method' for commands. Use 'add' or 'remove'.")
                        return
                    }
                }
            }

            else -> {
                sender.sendMessage("Unknown property '$property'. Available properties: max_redeems, max_per_player, enabled, command.")
                return
            }
        }

        val success = plugin.redeemCodeDB.upsert(redeemCode)
        if (success) {
            sender.sendMessage("Updated $property for code '${redeemCode.code}' to $value ")
        } else {
            sender.sendMessage("Failed to updated the code: ${redeemCode.code}")
            return
        }

    }
    private fun adjustDuration(existingDuration: String, adjustmentDuration: String, isAdding: Boolean): Long {
        val timeUnitToSeconds =
            mapOf("s" to 1L, "m" to 60L, "h" to 3600L, "d" to 86400L, "mo" to 2592000L, "y" to 31536000L)

        // Parse existing and adjustment durations
        val existingAmount = existingDuration.dropLast(1).toLongOrNull() ?: return 0L
        val existingUnit = existingDuration.takeLast(1)
        val adjustmentAmount = adjustmentDuration.dropLast(1).toLongOrNull() ?: return 0L
        val adjustmentUnit = adjustmentDuration.takeLast(1)

        // Convert both to seconds
        val existingSeconds = existingAmount * (timeUnitToSeconds[existingUnit] ?: return 0L)
        val adjustmentSeconds = adjustmentAmount * (timeUnitToSeconds[adjustmentUnit] ?: return 0L)

        // Perform addition or subtraction
        return if (isAdding) {
            existingSeconds + adjustmentSeconds
        } else {
            existingSeconds - adjustmentSeconds
        }
    }
}