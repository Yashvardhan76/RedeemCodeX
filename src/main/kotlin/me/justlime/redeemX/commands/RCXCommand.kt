package me.justlime.redeemX.commands

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.models.RedeemCode
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandException
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class RCXCommand(private val plugin: RedeemX) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /rxc <gen|delete|modify|info>")
            return true
        }
        if (sender.hasPermission("redeemx.admin")) {
            when (args[0].lowercase()) {
                "gen" -> handleGenerate(sender, args)
                "modify" -> handleModify(sender, args)
                "delete" -> handleDelete(sender, args)
                "delete_all" -> handleDeleteAll(sender, args)
                "info" -> handleInfo(sender)
                else -> sender.sendMessage("Unknown subcommand. Use 'gen', 'delete','delete_all', 'modify', or 'info'.")
            }
            return true
        }
        sender.sendMessage("${ChatColor.RED}You don't have permission to use this command.")
        return true
    }


    private fun handleGenerate(sender: CommandSender, args: Array<out String>) {
        if (args.size > 1) {
            var code = args[1]
//            val commands = args.slice(3 until args.size)
            val maxAttempts = plugin.config.getInt("max_attempts")
            // Check if codeNameOrSize is a number
            if (code.toIntOrNull() != null) {

                generateUniqueCode(code.toInt(), maxAttempts) { uniqueCode ->
                    if (uniqueCode != null) {
                        createRedeemCode(sender, uniqueCode)
                    } else {
                        // Handle the failure to generate a unique code
                        sender.sendMessage("Unable to generate a unique code of length ${code.toInt()}. Please try a different length or name. (Total $maxAttempts attempts)")
                    }
                }

            } else {
                code = code.uppercase()
                createRedeemCode(sender, code)
            }
        } else {
            sender.sendMessage("Usage: /rxc gen <code> <commands/template> <commands/template_name>")
        }
    }

    private fun createRedeemCode(sender: CommandSender, codeName: String) {
        // Check if code already exists
        sender.sendMessage("Hello $codeName!")
        if (plugin.redeemCodeDao.get(codeName) != null) {
            sender.sendMessage("The code '$codeName' already exists. Please choose a unique code.")
            return
        }

        // Create the redeem code with default or example values for the other fields
        val redeemCode = RedeemCode(
            code = codeName,
            commands = mutableMapOf(),
            storedTime = null,
            duration = null,
            isEnabled = false,
            maxRedeems = 1,
            maxPlayers = 1,
            permission = null,
            pin = -1,
            target = null,
            usage = mutableMapOf()
        )

        try {
            // Attempt to insert the code into the database
            val success = plugin.redeemCodeDao.insert(redeemCode)
            if (success) {
                sender.sendMessage("Code generated successfully: $codeName")
            } else {
                sender.sendMessage("Failed to generate the code.")
            }
        } catch (e: Exception) {
            sender.sendMessage("An error occurred while generating the code.")
            e.printStackTrace()
        }
    }


    private fun generateUniqueCode(length: Int, maxAttempts: Int = 1024, callback: (String?) -> Unit) {
        val charset = ('A'..'Z') + ('0'..'9')

        // Launch a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            var code: String
            var attempts = 0

            do {
                code = (1..length).map { charset.random() }.joinToString("")
                attempts++
                // Check if max attempts is reached
                if (attempts >= maxAttempts) {
                    withContext(Dispatchers.Main) { callback(null) } // Notify failure on the main thread
                    return@launch // Exit the coroutine
                }
            } while (plugin.redeemCodeDao.get(code) != null) // Ensure code is unique in DB

            // Return the unique code on the main thread
            withContext(Dispatchers.Main) { callback(code) }
        }
    }


    private fun handleDelete(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("Usage: /rxc delete <code>")
            return
        }

        val codeToDelete = args[1]

        // Attempt to find the redeem code by the provided code
        val redeemCode = plugin.redeemCodeDao.get(codeToDelete)

        if (redeemCode == null) {
            sender.sendMessage("The code '$codeToDelete' does not exist.")
            return
        }

        // Attempt to delete the redeem code from the database
        val success = plugin.redeemCodeDao.deleteByCode(codeToDelete)
        if (success) {
            sender.sendMessage("Successfully deleted the code: ${redeemCode.code}")
        } else {
            sender.sendMessage("Failed to delete the code: ${redeemCode.code}")
        }
    }

    private fun handleDeleteAll(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2 || args[1] != "CONFIRM") {
            sender.sendMessage("${ChatColor.YELLOW}Usage: /rxc delete_all CONFIRM")
            return
        }

        // Attempt to delete all redeem codes
        val success = plugin.redeemCodeDao.deleteAll()
        if (success) {
            sender.sendMessage("${ChatColor.GREEN}Successfully deleted all codes from the database.")
        } else {
            sender.sendMessage("${ChatColor.RED}Failed to delete all codes from the database.")
        }
    }


    private fun handleModify(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendMessage("Usage: /rxc modify <code> <property>")
            return
        }

        val code = args[1]
        val property = args[2].lowercase()

        if (property == "list") {
            sender.sendMessage(plugin.redeemCodeDao.get(code)?.toString())
            return
        }
        if (args.size < 4) {
            sender.sendMessage("Usage: /rxc modify <code> <property>")
            return
        }
        val value = args[3]

        // Attempt to find the redeem code by the provided code
        val redeemCode = plugin.redeemCodeDao.get(code)
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

                // Set the storedTime to current time if it hasn't been set
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
                val success = plugin.redeemCodeDao.update(redeemCode)
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

        val success = plugin.redeemCodeDao.update(redeemCode)
        if (success) {
            sender.sendMessage("Updated $property for code '${redeemCode.code}' to $value ")
        } else {
            sender.sendMessage("Failed to updated the code: ${redeemCode.code}")
            return
        }

    }

    private fun handleInfo(sender: CommandSender) {
        sender.sendMessage("RedeemX Plugin Version: ${plugin.description.version}")
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
