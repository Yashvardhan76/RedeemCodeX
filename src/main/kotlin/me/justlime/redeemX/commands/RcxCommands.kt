package me.justlime.redeemX.commands

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.models.RedeemCode
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class RedeemCommand(private val plugin: RedeemX) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /rxc <gen|delete|modify|info>")
            return true
        }

        when (args[0].lowercase()) {
            "gen" -> handleGenerate(sender, args)
            "modify" -> handleModify(sender, args)
            "delete" -> handleDelete(sender, args)
            "delete_all" -> handleDeleteAll(sender, args)
            "info" -> handleInfo(sender)
            else -> sender.sendMessage("Unknown subcommand. Use 'gen', 'delete', 'modify', or 'info'.")
        }
        return true
    }
    //addSubCmd("modify"){
    // addSubCmd("perms"){
    //
    // }
    //}
    //
    // }
    //
    //
    //

    private fun handleGenerate(sender: CommandSender, args: Array<out String>) {
        if (sender is Player && args.size > 1) {
            val code = args[1]
            val commands = args.slice(3 until args.size)
            val maxAttempts = plugin.config.getInt("max_attempts")

            // Check if codeNameOrSize is a number
            if (code.toIntOrNull() != null) {
                // Generate a random code with the specified number of digits
                generateUniqueCode(code.toInt(), maxAttempts) { uniqueCode ->
                    if (uniqueCode != null) {
                        createRedeemCode(sender, uniqueCode, commands)
                    } else {
                        // Handle the failure to generate a unique code
                        sender.sendMessage("Unable to generate a unique code of length ${code.toInt()}. Please try a different length or name. (Total $maxAttempts attempts)")
                    }
                }
            } else {
                createRedeemCode(sender, code, commands)
            }
        } else {
            sender.sendMessage("Usage: /rxc gen <code> <commands/template> <commands/template_name>")
        }
    }

    private fun createRedeemCode(sender: Player, codeName: String, commands: List<String>) {
        // Check if code already exists
        if (plugin.redeemCodeDao.findByCode(codeName) != null) {
            sender.sendMessage("The code '$codeName' already exists. Please choose a unique code.")
            return
        }

        // Create the redeem code with default or example values for the other fields
        val redeemCode = RedeemCode(
            code = codeName,
            commands = commands,
            maxRedeems = 1,
            maxPerPlayer = 1,
            isEnabled = true,
            expiry = null,
            permission = null,
            secureCode = null,
            specificPlayerId = null
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
            e.printStackTrace() // Log the error to the console for debugging
        }
    }

    private fun generateUniqueCode(length: Int, maxAttempts: Int = 1024, callback: (String?) -> Unit) {
        val charset = ('A'..'Z') + ('0'..'9')

        // Launch a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            var code: String
            var attempts = 0

            do {
                code = (1..length)
                    .map { charset.random() }
                    .joinToString("")
                attempts++
                // Check if max attempts is reached
                if (attempts >= maxAttempts) {
                    withContext(Dispatchers.Main) { callback(null) } // Notify failure on the main thread
                    return@launch // Exit the coroutine
                }
            } while (plugin.redeemCodeDao.findByCode(code) != null) // Ensure code is unique in DB

            // Return the unique code on the main thread
            withContext(Dispatchers.Main) { callback(code) }
        }
    }

    private fun handleDelete(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("Usage: /rxc delete <codeId>")
            return
        }

        val codeIdToDelete = args[1].toInt()

        // Attempt to find the redeem code by the provided code
        val redeemCode = plugin.redeemCodeDao.findById(codeIdToDelete)

        if (redeemCode == null) {
            sender.sendMessage("The code '$codeIdToDelete' does not exist.")
            return
        }

        // Attempt to delete the redeem code from the database
        val success = plugin.redeemCodeDao.deleteById(codeIdToDelete)
        if (success) {
            sender.sendMessage("Successfully deleted the code: ${redeemCode.code}")
        } else {
            sender.sendMessage("Failed to delete the code: ${redeemCode.code}")
        }
    }
    private fun handleDeleteAll(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2 || args[1] != "CONFIRM") {
            sender.sendMessage("${ChatColor.YELLOW}Usage: /rxc deleteall CONFIRM")
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
        if (args.size < 4) {
            sender.sendMessage("Usage: /rxc modify <codeId> <property> <value>")
            return
        }

        val codeIdToModify = args[1].toInt()
        val property = args[2]
        val value = args[3] // Assuming the value is the next argument, adjust if needed

        // Attempt to find the redeem code by the provided code
        val redeemCode = plugin.redeemCodeDao.findById(codeIdToModify)

        if (redeemCode == null) {
            sender.sendMessage("The code '$codeIdToModify' does not exist.")
            return
        }

        when (property.lowercase(Locale.getDefault())) {
            "max_redeems" -> {
                redeemCode.maxRedeems =
                    value.toIntOrNull() ?: return sender.sendMessage("Invalid value for maxRedeems.")
                sender.sendMessage("Updated maxRedeems for code '${redeemCode.code}' to ${redeemCode.maxRedeems}.")
            }

            "max_per_player" -> {
                redeemCode.maxPerPlayer =
                    value.toIntOrNull() ?: return sender.sendMessage("Invalid value for maxPerPlayer.")
                sender.sendMessage("Updated maxPerPlayer for code '$codeIdToModify' to ${redeemCode.maxPerPlayer}.")
            }

            "enabled" -> {
                redeemCode.isEnabled = value.lowercase() == "true"
                sender.sendMessage("Updated enabled status for code '$codeIdToModify' to ${redeemCode.isEnabled}.")
            }
            // Add more properties as needed, e.g. expiry, permission, etc.
            else -> {
                sender.sendMessage("Unknown property '$property'. Available properties: max_redeems, max_per_player, enabled.")
                return
            }
        }

        // Attempt to update the redeem code in the database
        val success = plugin.redeemCodeDao.update(redeemCode)
        if (success) {
            sender.sendMessage("Successfully modified the code: $codeIdToModify")
        } else {
            sender.sendMessage("Failed to modify the code: $codeIdToModify")
        }
    }


    private fun handleInfo(sender: CommandSender) {
        sender.sendMessage("RedeemX Plugin Version: ${plugin.description.version}")
    }
}
