package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX

class CommandManager(plugin: RedeemX) {
    init {
        val redeemCommand = RedeemCommand(plugin)
        val rcxCommand = RCXCommand(plugin)

        // Register "rcx" command
        plugin.getCommand("rcx")?.apply {
            setExecutor(rcxCommand)
            tabCompleter = TabCompleterList(plugin)
        }

        // Register "redeem" command
        plugin.getCommand("redeem")?.apply {
            setExecutor(redeemCommand)
            tabCompleter = redeemCommand
        }
    }
}
