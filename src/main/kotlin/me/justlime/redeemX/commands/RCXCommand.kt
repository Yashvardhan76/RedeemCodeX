package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.DeleteSubCommand
import me.justlime.redeemX.commands.subcommands.GenerateSubCommand
import me.justlime.redeemX.commands.subcommands.InfoSubCommand
import me.justlime.redeemX.commands.subcommands.ModifySubCommand
import me.justlime.redeemX.commands.subcommands.RenewSubCommand
import me.justlime.redeemX.config.ConfigManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RCXCommand(private val plugin: RedeemX) : CommandExecutor {
    private val config = ConfigManager(plugin)
    private val stateManager = plugin.stateManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val stateManager = plugin.stateManager
        val state = stateManager.createState(sender)
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /rxc <gen|delete|modify|info>")
            return true
        }
        if (sender.hasPermission("redeemx.admin.use")) {
            when (args[0].lowercase()) {
                "gen" -> if (sender.hasPermission("redeemx.admin.use.gen")) {
                    GenerateSubCommand(plugin).execute(sender, args)
                } else config.sendMessage("no-permission", state)

                "modify", "modify_template" -> if (sender.hasPermission("redeemx.admin.use.modify")) {
                    ModifySubCommand(plugin).execute(sender, args)
                } else config.sendMessage("no-permission", state)

                "delete", "delete_all", "delete_template", "delete_all_template" -> if (sender.hasPermission("redeemx" + ".admin.use" + ".delete")) {
                    DeleteSubCommand(plugin).execute(sender, args)
                } else config.sendMessage("no-permission", state)

                "info" -> if (sender.hasPermission("redeemx.admin.use.info")) InfoSubCommand(plugin).execute(sender)
                else config.sendMessage(key = "no-permission", state)

                "renew" -> if (sender.hasPermission("redeemx.admin.use.renew")) {
                    RenewSubCommand(plugin).execute(sender, args)
                } else config.sendMessage("no-permission", state)

                "reload" -> if (sender.hasPermission("redeemx.admin.use.reload")) {
                    config.reloadAllConfigs()
                    config.sendMessage("commands.reload", state)
                } else config.sendMessage("no-permission", state)

                else -> sender.sendMessage(config.getString("commands.unknown-command"))
            }
            return true
        }
        config.sendMessage("no-permission", state)
        return true
    }

}
