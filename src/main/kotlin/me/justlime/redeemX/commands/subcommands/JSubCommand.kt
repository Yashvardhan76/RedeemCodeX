package me.justlime.redeemX.commands.subcommands

interface JSubCommand {
    fun execute(sender: org.bukkit.command.CommandSender, args: Array<out String>): Boolean
}
