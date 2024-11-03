package me.justlime.redeemX.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class CommandHandler(
    private val plugin: JavaPlugin,
    rootName: String,
    rootAction: (sender: CommandSender, args: List<String>) -> Boolean
) : CommandExecutor {

    private val rootCommand: SubCommand = SubCommand(rootName, action = rootAction)

    fun addSubCommand(
        name: String,
        usePermission: Boolean = false,
        action: (sender: CommandSender, args: List<String>) -> Boolean
    ): SubCommand {
        return rootCommand.addSubCmd(name, usePermission, action)
    }

    fun register() {
        val command = plugin.getCommand(rootCommand.name)
        if (command != null) {
            command.setExecutor(this)
        } else {
            plugin.logger.warning("Command ${rootCommand.name} not found in plugin.yml!")
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return rootCommand.execute(sender, args.toList())
    }
}
