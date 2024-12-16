package me.justlime.redeemX.enums

import org.bukkit.command.CommandSender

interface JSubCommand {
    fun execute(sender: CommandSender,args: MutableList<String>): Boolean
}
