package me.justlime.redeemX.commands

import org.bukkit.command.CommandSender

class SubCommand(
    val name: String? = null,
    private val usePermission: Boolean = true,
    val action: (sender: CommandSender, args: List<String>) -> Boolean
) {
    private val subCommands = mutableMapOf<String?, SubCommand>()
    private var counterCommand: Int = 0

    private val permission: String
        get() = "redeemx.$name"

    fun addSubCmd(
        name: String? = null,
        usePermission: Boolean = true,
        action: (sender: CommandSender, args: List<String>) -> Boolean = { _, _ -> false }
    ): SubCommand {
            val subCmd = SubCommand(name, usePermission, action)
            subCommands[name] = subCmd
            counterCommand++
            return subCmd
    }

    fun execute(sender: CommandSender, args: List<String>): Boolean {
        // Check if the sender has permission to execute this command
        if (usePermission && !sender.hasPermission(permission)) {
            sender.sendMessage("§cYou do not have permission to use this command: $permission")
            return false
        }

        // Run the command if there are no more arguments
        if (args.isEmpty()) {
            return action(sender, args)
        }

        // Retrieve the subcommand by name (args[0])
        val subCommand = subCommands[args[0]]
        return if (subCommand != null) {
            // Delegate execution to the subcommand with the remaining args
            subCommand.execute(sender, args.drop(1))
        } else {
            sender.sendMessage("§cUnknown subcommand: ${args[0]}")
            false
        }
    }

    // Tab completion method
    fun tabComplete(sender: CommandSender, args: List<String>): List<String>? {
        val completions = mutableListOf<String>()

        if (usePermission && !sender.hasPermission(permission)) {
            return completions
        }

        if (args.isEmpty()) {
            // Suggest all available subcommands
            completions.addAll(subCommands.values.filter { it.canSuggest(sender) }.mapNotNull { it.name })
        } else {
            val subCommand = subCommands[args[0]]
            if (subCommand != null) {
                return subCommand.tabComplete(sender, args.drop(1))
            }else {
                // Suggest subcommands matching the current input prefix
                val completions = subCommands.values.filter {
                    it.name?.startsWith(args[0], ignoreCase = true) == true && it.canSuggest(sender)
                }.mapNotNull { it.name }

                return if (completions.isNotEmpty()) completions else null // Return null if no completions found
            }
        }
        return completions
    }

    // Helper method to check if a command suggestion can be shown to the sender
    private fun canSuggest(sender: CommandSender): Boolean {
        return !usePermission || sender.hasPermission(permission)
    }
}
