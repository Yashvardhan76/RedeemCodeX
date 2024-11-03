package me.justlime.redeemX.commands

import org.bukkit.command.CommandSender

class SubCommand(
    val name: String,
    val usePermission: Boolean = false, // Use permission check automatically based on the command name
    val action: (sender: CommandSender, args: List<String>) -> Boolean
) {
    private val subCommands = mutableMapOf<String, SubCommand>()

    // Automatically derive the permission string from the command name
    private val permission: String
        get() = "redeemx.$name"

    fun addSubCmd(
        name: String,
        usePermission: Boolean = false,
        action: (sender: CommandSender, args: List<String>) -> Boolean
    ): SubCommand {
        val subCmd = SubCommand(name, usePermission, action)
        subCommands[name] = subCmd
        return subCmd
    }

    fun execute(sender: CommandSender, args: List<String>): Boolean {
        // Check if the sender has permission, if usePermission is enabled
        if (usePermission && !sender.hasPermission(permission)) {
            sender.sendMessage("§cYou do not have permission to use this command: $permission")
            return false
        }

        // Execute the current action if no subcommands are left
        if (args.isEmpty()) {
            return action(sender, args)
        }

        // Attempt to execute the next subcommand
        val subCommand = subCommands[args[0]]
        return if (subCommand != null) {
            subCommand.execute(sender, args.drop(1))
        } else {
            sender.sendMessage("§cUnknown subcommand: ${args[0]}")
            false
        }
    }
}
