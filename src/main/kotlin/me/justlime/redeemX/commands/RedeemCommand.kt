package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

class RedeemCommand(private val plugin: RedeemX) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be run by players")
            return true
        }
        if (args.isEmpty()) return false
        val code = args[0].uppercase(Locale.getDefault())
        if (plugin.redeemCodeDao.findByCode(code) == null) {
            sender.sendMessage("The Code Doesn't Exist")
            return true
        }
        if (!plugin.redeemCodeDao.isExpired(code)) {
            sender.sendMessage("The Code has been redeemed")
            return true
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        val completion = emptyList<String>().toMutableList()
        return completion
    }
}