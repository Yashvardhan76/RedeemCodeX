package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.data.models.RedeemCode
import org.bukkit.command.CommandSender

class GenerateSubCommand(private val plugin: RedeemX) {
    private val config = ConfigManager(plugin)

    fun execute(sender: CommandSender, args: Array<out String>) {
        config.setState(sender)
        if (!sender.hasPermission("redeemx.use.gen")) {
            sender.sendMessage(config.getString("no-permission"))
            return
        }

        if (args.size <= 1) {
            sender.sendMessage(config.getString("commands.gen.invalid-syntax"))
            return
        }
        val code = args[1]
        val maxAttempts = plugin.config.getInt("max-attempts")
        if (code.toIntOrNull() == null) {
            createRedeemCode(sender, code.uppercase())
            return
        }
        generateUniqueCode(code.toInt(), maxAttempts) { uniqueCode ->
            if (uniqueCode == null) {
                sender.sendMessage(config.getString("commands.gen.length-error"))
                return@generateUniqueCode
            }
            createRedeemCode(sender, uniqueCode)
        }
        return

    }

    private fun createRedeemCode(sender: CommandSender, codeName: String) {
        // Check if code already exists
        if (plugin.redeemCodeDB.get(codeName) != null) {
            sender.sendMessage(
                config.getString(
                    "commands.gen.code-already-exist"
                )
            )
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
            val success = plugin.redeemCodeDB.upsert(redeemCode)
            if (success) {
                sender.sendMessage(config.getString("commands.gen.success"))
            } else {
                sender.sendMessage(config.getString("commands.gen.failed"))
            }
        } catch (e: Exception) {
            sender.sendMessage(config.getString("commands.gen.error"))
            e.printStackTrace()
        }
    }

    private fun generateUniqueCode(length: Int, maxAttempts: Int = 1024, callback: (String?) -> Unit) {
        val charset = ('A'..'Z') + ('0'..'9')
        var code: String
        var attempts = 0
        do {
            code = (1..length).map { charset.random() }.joinToString("")
            attempts++
            // Check if max attempts is reached
            if (attempts >= maxAttempts) {
                return callback(null)
            }
        } while (plugin.redeemCodeDB.get(code) != null) // Ensure code is unique in DB
        callback(code)
    }
}