package me.justlime.redeemX.enums

import org.bukkit.command.CommandSender

interface JSubCommand {
    var codeList: List<String>
    fun execute(sender: CommandSender,args: MutableList<String>): Boolean
}
