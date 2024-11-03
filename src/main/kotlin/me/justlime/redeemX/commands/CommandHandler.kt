package me.justlime.redeemX.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin

class CommandHandler(
    private val plugin: JavaPlugin,
    rootName: String,
    rootAction: (sender: CommandSender, args: List<String>) -> Boolean = { _, _ -> false}
) : CommandExecutor, TabCompleter {

    private val rootCommand: SubCommand = SubCommand(rootName, action = rootAction)

    fun addSubCommand(
        name: String,
        usePermission: Boolean = true,
        action: (sender: CommandSender, args: List<String>) -> Boolean = { _, _ -> false }
    ): SubCommand {
        return rootCommand.addSubCmd(name, usePermission, action)
    }

    fun register() {
        val command = rootCommand.name?.let { plugin.getCommand(it) }
        if (command != null) {
            command.setExecutor(this)
        } else {
            plugin.logger.warning("Command ${rootCommand.name} not found in plugin.yml!")
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return rootCommand.execute(sender, args.toList())
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        return rootCommand.tabComplete(sender, args.toList())
    }
}
