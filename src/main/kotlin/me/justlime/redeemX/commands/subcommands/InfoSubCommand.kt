package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.enums.JSubCommand
import org.bukkit.command.CommandSender

class InfoSubCommand(private val plugin: RedeemX): JSubCommand {
    override var codeList: List<String> = emptyList()
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        sender.sendMessage("RedeemX Plugin Version: ${plugin.description.version}")
        return true
    }
}