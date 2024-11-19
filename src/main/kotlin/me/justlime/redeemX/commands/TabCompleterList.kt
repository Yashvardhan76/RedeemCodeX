package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TabCompleterList(plugin: RedeemX) : TabCompleter {

    // Predefined common completions for each command type
    private val config = ConfigManager(plugin)

    private val commonCompletions = listOf(
        "gen", "modify", "delete", "delete_all", "info","renew"
    )

    private val commonCompletionsTODO = listOf(   //TODO
        "bulk_gen", "delete_Expired", "renew", "preview", "reload", "help"
    )
    private val modifyOptions = listOf(
        "enabled", "max_redeems", "max_player", "duration", "permission", "set_target", "set_pin", "command", "list"
    )
    private val genSubcommands = listOf("CUSTOM", "SIZE","TEMPLATE")
    private val durationOptions = listOf("add", "set", "remove")
    private val permissionOptions = listOf("true", "false", "CUSTOM")
    private var cachedCodes: List<String>? = plugin.redeemCodeDB.getFetchCodes

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): MutableList<String>? {
        val completions: MutableList<String> = mutableListOf()

        // Handle argument completions based on argument size
        when (args.size) {
            1 -> completions.addAll(commonCompletions)
            2 -> completions.addAll(handleSecondArgument(args[0]))
            3 -> completions.addAll(handleThirdArgument(args))
            4 -> handleFourthArgument(args)?.let { completions.addAll(it) }
        }

        // Filter and return completions that match the current input (case-insensitive)
        return completions.filter { it.startsWith(args.lastOrNull() ?: "", ignoreCase = true) }
            .sortedBy { it.lowercase() }.toMutableList()
    }

    private fun handleSecondArgument(firstArg: String): List<String> {
        return when (firstArg) {
            "gen" -> genSubcommands
            "delete_all" -> emptyList()
            else -> cachedCodes ?: emptyList() // Use cached codes
        }
    }

    private fun handleThirdArgument(args: Array<out String>): List<String> {
        return when (args[0]) {
            "gen" -> config.getTemplateNames() // Add template subcommand for 'gen'
            "modify" -> modifyOptions
            else -> emptyList()
        }
    }

    private fun handleFourthArgument(args: Array<out String>): List<String>? {
        return when (args[2]) {
            "duration" -> durationOptions
            "enabled" -> listOf("true", "false")
            "permission" -> permissionOptions
            "set_pin" -> listOf("-1")
            "command" -> listOf("add", "set", "list", "preview")
            "set_target" -> null
            else -> emptyList()
        }
    }
}
