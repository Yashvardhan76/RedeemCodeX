package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.DeleteSubCommand
import me.justlime.redeemX.commands.subcommands.GenerateSubCommand
import me.justlime.redeemX.commands.subcommands.InfoSubCommand
import me.justlime.redeemX.commands.subcommands.ModifySubCommand
import me.justlime.redeemX.commands.subcommands.RenewSubCommand
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RCXCommand(private val plugin: RedeemX) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /rxc <gen|delete|modify|info>")
            return true
        }
        if (sender.hasPermission("redeemx.admin")) {
            when (args[0].lowercase()) {
                "gen" -> GenerateSubCommand(plugin).execute(sender, args)
                "modify" -> ModifySubCommand(plugin).execute(sender, args)
                "delete" -> DeleteSubCommand(plugin).execute(sender, args)
                "delete_all" -> DeleteSubCommand(plugin).execute(sender, args)
                "info" -> InfoSubCommand(plugin).execute(sender)
                "renew" -> RenewSubCommand(plugin).execute(sender,args)
                "reload" -> {
                    plugin.configFile.reloadConfig()
                    sender.sendMessage("Plugin Reloaded")
                    return true
                }
                else -> sender.sendMessage("Unknown subcommand. Use 'gen', 'delete','delete_all', 'modify', or 'info'.")
            }
            return true
        }
        sender.sendMessage("${ChatColor.RED}You don't have permission to use this command.")
        return true
    }





}
