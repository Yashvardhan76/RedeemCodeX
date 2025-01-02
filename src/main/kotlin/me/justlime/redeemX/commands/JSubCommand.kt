package me.justlime.redeemX.commands

import org.bukkit.command.CommandSender

interface JSubCommand {
    var jList: List<String> // Stores the list of codes for the command
    val permission: String // Defines the required permission for this command

    /**
     * Executes the command logic.
     *
     * @param sender The entity executing the command.
     * @param args The arguments passed with the command.
     * @return True if the command executed successfully, otherwise false.
     */
    fun execute(sender: CommandSender, args: MutableList<String>): Boolean

    fun sendMessage(key: String): Boolean

    /**
     * Checks if the sender has the required permission.
     *
     * @param sender The entity executing the command.
     * @return True if the sender has permission, otherwise false.
     */
    fun hasPermission(sender: CommandSender): Boolean {
        return sender.hasPermission(permission)
    }

    fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String>?
}

