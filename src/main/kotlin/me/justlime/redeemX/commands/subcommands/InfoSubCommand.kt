package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import org.bukkit.command.CommandSender

class InfoSubCommand(private val plugin: RedeemX) {
    fun execute(sender: CommandSender) {
        sender.sendMessage("RedeemX Plugin Version: ${plugin.description.version}")
    }
}