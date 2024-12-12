package me.justlime.redeemX.commands.subcommands

import org.bukkit.command.CommandSender

interface JSubCommand {
    fun execute(sender: CommandSender,args: MutableList<String>): Boolean
}
