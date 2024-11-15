package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.data.state.RedeemCodeState
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class RedeemCommand(private val plugin: RedeemX) : CommandExecutor, TabCompleter {
    private val config = ConfigManager(plugin)
    private lateinit var state: RedeemCodeState

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        config.setState(sender)
        state = config.state

        state.sender = sender
        if (sender !is Player) {
           config.sendMessage("restricted-to-players")
            return true
        }
        state.sender = sender
        if (args.isEmpty()) {
            config.sendMessage("redeemed-message.usage",state)
            return true
        }

        state.inputCode = args[0].uppercase()
        val redeemCodeDao = plugin.redeemCodeDB
        val codeState = redeemCodeDao.get(state.inputCode)

        if (codeState == null) {
            config.sendMessage("redeemed-message.invalid-code")
            return true
        }

        config.setState(sender, codeState)

        if (!state.permission.isNullOrBlank() && !sender.hasPermission(state.permission!!)) {
            config.sendMessage("redeemed-message.no-permission")
            return true
        }

        if (!state.isEnabled) {
            config.sendMessage("redeemed-message.disabled")
            return true
        }
        if (redeemCodeDao.isExpired(state.inputCode)) {
            config.sendMessage("redeemed-message.expired-code")
            return true
        }

        if (state.pin >= 0) {
            if (args.size < 2) {
                config.sendMessage("redeemed-message.missing-pin")
                return true
            }

            state.inputPin = args[1].toIntOrNull()
            if (state.inputPin != state.pin) {
                config.sendMessage("redeemed-message.invalid-pin")
                return true
            }
        }

        state.usageCount = state.usage[sender.name] ?: 0
        if (state.usageCount > state.maxRedeems) {
            config.sendMessage("redeemed-message.already-redeemed")
            return true
        }

        if (state.usage.size > state.maxPlayers) {
            config.sendMessage("redeemed-message.max-redemptions")
            return true
        }

        if (!state.target.isNullOrEmpty() && state.target != sender.name) {
            config.sendMessage("redeemed-message.invalid-target")
            return true
        }

        val console = plugin.server.consoleSender
        state.commands.values.forEach {
            plugin.server.dispatchCommand(console, it)
        }

        state.usage[sender.name] = state.usageCount + 1
        codeState.usage = state.usage

        if (!redeemCodeDao.upsert(codeState)) {
            config.sendMessage("redeemed-message.failed")
            return true
        }
        config.sendMessage("redeemed-message.success")
        config.setState(sender, codeState)
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        return emptyList()
    }
}
