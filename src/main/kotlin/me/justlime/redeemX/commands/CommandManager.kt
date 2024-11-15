package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.state.StateManager

class CommandManager(plugin: RedeemX, stateManager: StateManager) {
    init {
        val redeemCommand = RedeemCommand(plugin, stateManager)
        val rcxCommand = RCXCommand(plugin, stateManager)

        // Register "rcx" command
        plugin.getCommand("rcx")?.apply {
            setExecutor(rcxCommand)
            tabCompleter = TabCompleterList(plugin) // If specific TabCompleter is needed
        }

        // Register "redeem" command
        plugin.getCommand("redeem")?.apply {
            setExecutor(redeemCommand)
            tabCompleter = redeemCommand // Reuse the same instance for tab completion
        }
    }
}
