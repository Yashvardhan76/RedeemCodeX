package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX

class CommandManager(plugin: RedeemX) {
    init {
        plugin.getCommand("rcx")?.setExecutor(RCXCommand(plugin))
        plugin.getCommand("rcx")?.tabCompleter = TabCompleterList(plugin)
        plugin.getCommand("redeem")?.setExecutor(RedeemCommand(plugin))
        plugin.getCommand("redeem")?.tabCompleter = RedeemCommand(plugin)
    }
}