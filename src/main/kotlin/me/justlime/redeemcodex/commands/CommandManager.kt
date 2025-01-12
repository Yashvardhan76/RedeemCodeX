/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 *
 */


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
