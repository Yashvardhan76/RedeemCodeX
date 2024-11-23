package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class RedeemCommand(private val plugin: RedeemX) : CommandExecutor, TabCompleter {

    private val stateManager = plugin.stateManager
    private val config = ConfigManager(plugin)
    private val service = RedeemCodeService(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Initialize state for the sender
        val state = stateManager.createState(sender)

        if (sender !is Player) {
            config.sendMessage("restricted-to-players", state)
            return true
        }

        // Validate input arguments
        if (args.isEmpty()) {
            config.sendMessage("redeemed-message.usage", state)
            return true
        }

        // Update state with input code
        state.inputTemplate = args[0].uppercase()

        if (!stateManager.fetchState(sender, state.inputTemplate)) {
            config.sendMessage("redeemed-message.invalid-code", state)
            return true
        }

        state.usageCount = state.usage[sender.name] ?: 0
        if (state.usageCount >= state.maxRedeems) {
            config.sendMessage("redeemed-message.already-redeemed", state)
            return true
        }

        if (state.usage.size > state.maxPlayers) {// Max players check
            config.sendMessage("redeemed-message.max-redemptions", state)
            return true
        }

        if (!state.permission.isNullOrBlank() && !sender.hasPermission(state.permission!!)) {
            config.sendMessage("redeemed-message.no-permission", state)
            return true
        }

        if (!state.isEnabled) {
            config.sendMessage("redeemed-message.disabled", state)
            return true
        }

        if (service.isExpired(state.inputTemplate)) {
            config.sendMessage("redeemed-message.expired-code", state)
            return true
        }

        // Target validation
        val tempString = state.target.toString().removeSurrounding("[", "]").trim()
        if (tempString.isNotBlank()) {
            val temp: MutableList<String?> = mutableListOf();
            state.target.filterNotNull().toMutableList().forEach {
                temp.add(it.trim())
            }
            state.target = temp
            if (!state.target.contains(sender.name)) {
                config.sendMessage("redeemed-message.invalid-target", state)
                return true
            }
        }

        if (state.pin >= 0) {
            if (args.size < 2) {
                config.sendMessage("redeemed-message.missing-pin", state)
                return true
            }

            state.inputPin = args[1].toIntOrNull()
            if (state.inputPin != state.pin) {
                config.sendMessage("redeemed-message.invalid-pin", state)
                return true
            }
        }

        // Execute commands
        val console = plugin.server.consoleSender
        state.commands.values.forEach {
            plugin.server.dispatchCommand(console, it)
        }

        state.usage[sender.name] = state.usageCount + 1
        val success = stateManager.updateDb(sender)
        if (!success) {
            config.sendMessage("redeemed-message.failed", state)
            return true
        }

        // Success message
        config.sendMessage("redeemed-message.success", state)
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): List<String> {
        return emptyList()
    }

}
