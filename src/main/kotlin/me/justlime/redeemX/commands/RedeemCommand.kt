package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class RedeemCommand(private val plugin: RedeemX) : CommandExecutor, TabCompleter {
    val config = ConfigManager(plugin).config
    val messages = ConfigManager(plugin).message

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(
                messages.getString("restricted-to-players")
            )
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("Usage: /redeem <code> [pin]")
            return true
        }

        val senderCode = args[0].uppercase()
        val redeemCodeDao = plugin.redeemCodeDB
        val code = redeemCodeDao.get(senderCode)

        if (code == null) {
            sender.sendMessage("The code '$senderCode' does not exist.")
            return true
        }

        if (!code.permission.isNullOrBlank() && !sender.hasPermission(code.permission!!)) {
            sender.sendMessage("You don't have permission to use this command.")
            return true
        }

        if (!code.isEnabled) {
            sender.sendMessage("The code $senderCode is not enabled.")
            return true
        }

        if (redeemCodeDao.isExpired(senderCode)) {
            sender.sendMessage("The code '$senderCode' has expired.")
            return true
        }

        if (code.pin >= 0) {
            if (args.size < 2) {
                sender.sendMessage("This code requires a pin. Usage: /redeem <code> <pin>")
                return true
            }

            val inputPin = args[1].toIntOrNull()
            if (inputPin != code.pin) {
                sender.sendMessage("The pin is incorrect.")
                return true
            }
        }

        // Check redemption limits
        val usageCount = code.usage[sender.name] ?: 0
        if (usageCount >= code.maxRedeems) {
            sender.sendMessage("You have reached the maximum redemption limit for this code.")
            return true
        }

        //Check Maximum Player Limit
        if (code.usage.size > code.maxPlayers) {
            sender.sendMessage("The maximum number of players have redeemed this code.")
            return true
        }

        // Check if code has a specific target player
        if (!code.target.isNullOrEmpty() && code.target != sender.name) {
            sender.sendMessage("This code can only be redeemed by ${code.target}.")
            return true
        }

        // Redeem the code
        val console = plugin.server.consoleSender
        code.commands.values.forEach {
            plugin.server.dispatchCommand(console, it)
        }

        code.usage[sender.name] = usageCount + 1
        val success = redeemCodeDao.upsert(code)

        if (success) {
            sender.sendMessage("You have successfully redeemed the code '$senderCode'.")
        } else {
            sender.sendMessage("Failed to redeem the code. Please try again later.")
        }

        return true
    }

    //To Avoid Getting Player Names
    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): MutableList<String> {
        return mutableListOf()
    }
}
