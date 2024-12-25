package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX

class CommandManager(plugin: RedeemX) {
    val tabCompleterList = TabCompleterList(plugin)
    private val redeemCommand = RedeemCommand(plugin)
    private val rcxCommand = RCXCommand(plugin)
    init {
        // Register "rcx" command
        plugin.getCommand("rcx")?.apply {
            setExecutor(rcxCommand)
            tabCompleter = tabCompleterList
            tabCompleterList.fetched()
        }

        // Register "redeem" command
        plugin.getCommand("redeem")?.apply {
            setExecutor(redeemCommand)
            tabCompleter = redeemCommand
        }
    }
}
