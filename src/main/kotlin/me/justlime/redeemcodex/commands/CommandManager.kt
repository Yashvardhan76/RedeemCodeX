package me.justlime.redeemcodex.commands

import me.justlime.redeemcodex.RedeemCodeX

class CommandManager(plugin: RedeemCodeX) {
    val tabCompleterList = RCXCommand(plugin)
    private val redeemCommand = RedeemCommand(plugin)
    private val rcxCommand = RCXCommand(plugin)
    init {
        // Register "rcx" command
        plugin.getCommand("rcx")?.apply {
            setExecutor(rcxCommand)
            tabCompleter = tabCompleterList
        }

        // Register "redeem" command
        plugin.getCommand("redeem")?.apply {
            setExecutor(redeemCommand)
            tabCompleter = redeemCommand
        }
    }
}
