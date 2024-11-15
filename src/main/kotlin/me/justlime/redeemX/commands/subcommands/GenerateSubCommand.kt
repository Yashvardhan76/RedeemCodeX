package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.state.RedeemCodeState
import me.justlime.redeemX.state.StateManager
import org.bukkit.command.CommandSender

class GenerateSubCommand(private val plugin: RedeemX, private val stateManager: StateManager) {
    private val config = ConfigManager(plugin, stateManager = stateManager)

    fun execute(sender: CommandSender, args: Array<out String>) {
        // Retrieve or create a state for the sender
        val state = stateManager.getOrCreateState(sender)

        // Check permissions
        if (!sender.hasPermission("redeemx.use.gen")) {
            config.sendMessage("no-permission", state)
            return
        }

        // Validate arguments
        if (args.size <= 1) {
            config.sendMessage("commands.gen.invalid-syntax", state)
            return
        }

         state.inputCode = args[1].uppercase()
        val maxAttempts = plugin.config.getInt("max-attempts")

        if ( state.inputCode.toIntOrNull() == null) {
            createRedeemCode(state)
            return
        }

        generateUniqueCode( state.inputCode.toInt(), maxAttempts) { uniqueCode ->
            if (uniqueCode == null) {
                config.sendMessage("commands.gen.length-error", state)
                return@generateUniqueCode
            }
            state.inputCode = uniqueCode
            createRedeemCode(state)
        }
    }

    private fun createRedeemCode(state: RedeemCodeState) {
        // Check if code already exists
        if (plugin.redeemCodeDB.get(state.inputCode) != null) {
            config.sendMessage("commands.gen.code-already-exist", state)
            return
        }

        // Create the redeem code
        val redeemCode = RedeemCode(
            code = state.inputCode,
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
            // Insert the redeem code into the database
            val success = plugin.redeemCodeDB.upsert(redeemCode)
            if (success) {
                config.sendMessage("commands.gen.success", state)
            } else {
                config.sendMessage("commands.gen.failed", state)
            }
        } catch (e: Exception) {
            config.sendMessage("commands.gen.error", state)
            e.printStackTrace()
        }
    }

    private fun generateUniqueCode(length: Int, maxAttempts: Int = 1024, callback: (String?) -> Unit) {
        val charset = ('A'..'Z') + ('0'..'9')
        var attempts = 0
        do {
            val code = (1..length).map { charset.random() }.joinToString("")
            attempts++

            // Stop if max attempts are exceeded
            if (attempts >= maxAttempts) {
                callback(null)
                return
            }

            // Ensure the generated code is unique
            if (plugin.redeemCodeDB.get(code) == null) {
                callback(code)
                return
            }
        } while (true)
    }
}
