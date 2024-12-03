package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.config.JMessage
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
        var state = stateManager.getState(sender)
        if (sender !is Player) {
            config.dm(JMessage.RESTRICTED_TO_PLAYERS, state)
            return true
        }
        if (args.isEmpty()) {
            config.dm(JMessage.Redeemed.USAGE, state)
            return true
        }

        state.inputCode = args[0].uppercase()
        if (!stateManager.fetchState(state)) {
            config.dm(JMessage.Redeemed.INVALID_CODE, state)
            return true
        }
        plugin.logger.info("RedeemCommand state: $state")

        state.usageCount = state.usage[sender.name] ?: 0
        if (state.usageCount >= state.maxRedeems) {
            config.dm(JMessage.Redeemed.MAX_REDEMPTIONS, state)
            return true
        }

        if (state.usage.size > state.maxPlayers) {
            config.dm(JMessage.Redeemed.MAX_PLAYER_REDEEMED, state)
            return true
        }

        if (!state.permission.isNullOrBlank() && !sender.hasPermission(state.permission!!)) {
            config.dm(JMessage.Redeemed.NO_PERMISSION, state)
            return true
        }

        if (!state.isEnabled) {
            config.dm(JMessage.Redeemed.DISABLED, state)
            return true
        }

        if (service.isExpired(state.inputTemplate)) {
            config.dm(JMessage.Redeemed.EXPIRED_CODE, state)
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
                config.dm(JMessage.Redeemed.INVALID_TARGET, state)
                return true
            }
        }

        if (state.pin >= 0) {
            if (args.size < 2) {
                config.dm(JMessage.Redeemed.MISSING_PIN, state)
                return true
            }

            state.inputPin = args[1].toIntOrNull()
            if (state.inputPin != state.pin) {
                config.dm(JMessage.Redeemed.INVALID_PIN, state)
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
            config.dm(JMessage.Redeemed.FAILED, state)
            return true
        }

        // Success message
        config.dm(JMessage.Redeemed.SUCCESS, state)
        return true
    }





    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): List<String> {
        return emptyList()
    }

}
