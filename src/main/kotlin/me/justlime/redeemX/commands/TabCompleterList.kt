package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.ConfigManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TabCompleterList(val plugin: RedeemX) : TabCompleter {
    // Predefined common completions for each command type
    private val config = ConfigManager(plugin)

    private val commonCompletions = listOf(
        "gen", "modify","modify_template","delete", "delete_all", "info", "renew", "reload"
    )

    private val commonCompletionsTODO = listOf(   //TODO
        "delete_Expired", "preview", "help"
    )
    private val modifyOptions = listOf(
        "enabled", "max_redeems", "max_player", "duration", "permission", "target", "set_pin", "command", "list"
    )
    private val modifyTemplateOptions = listOf(
        "enabled", "max_redeems", "max_player", "duration", "permission", "set_pin", "command", "list"
    )
    private val amount = listOf("AMOUNT")
    private val genSubcommands = listOf("CUSTOM", "SIZE", "TEMPLATE")
    private val durationOptions = listOf("add", "set", "remove")
    private val permissionOptions = listOf("true", "false", "CUSTOM")
    private var cachedCodes = plugin.redeemCodeDB

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): MutableList<String>? {
        val completions: MutableList<String> = mutableListOf()

        // Handle argument completions based on argument size
        when (args.size) {
            1 -> completions.addAll(commonCompletions)
            2 -> completions.addAll(handleSecondArgument(args[0]))
            3 -> completions.addAll(handleThirdArgument(args))
            4 -> {
                handleFourthArgument(args).let { completions.addAll(it) }

            }

            5 -> {
                handleFifthArgument(args)?.let { completions.addAll(it) }
                if (args[2].equals("target", ignoreCase = true) && (args[3].equals(
                        "add",
                        ignoreCase = true
                    ) or args[3].equals("set", ignoreCase = true))
                ) return null

            }

        }

        // Filter and return completions that match the current input (case-insensitive)
        return completions.filter { it.startsWith(args.lastOrNull() ?: "", ignoreCase = true) }
            .sortedBy { it.lowercase() }.toMutableList()
    }

    private fun handleSecondArgument(firstArg: String): List<String> {
        return when (firstArg) {
            "gen" -> genSubcommands
            "delete_all" -> emptyList()
            "delete" -> cachedCodes.getFetchCodes
            "info" -> emptyList()
            "renew" -> cachedCodes.getFetchCodes
            "modify" -> cachedCodes.getFetchCodes
            "modify_template" -> config.getTemplateNames()
            "reload" -> emptyList()
            else -> emptyList()
        }
    }

    private fun handleThirdArgument(args: Array<out String>): List<String> {
        return when (args[0]) {
            "gen" -> if (!args[1].equals(
                    "template", ignoreCase = true
                )
            ) return amount else return config.getTemplateNames()
            // template
            // subcommand for 'gen'
            "modify" -> modifyOptions
            "modify_template" -> modifyTemplateOptions
            else -> emptyList()
        }
    }

    private fun handleFourthArgument(args: Array<out String>): List<String> {
        return when (args[2]) {
            "duration" -> durationOptions
            "enabled" -> listOf("true", "false")
            "permission" -> permissionOptions
            "pin" -> listOf("-1")
            "command" -> listOf("add", "set", "list", "preview")
            "target" -> if (args[0].equals("modify", ignoreCase = true)) listOf("add", "remove", "remove_all", "set",
                "list") else emptyList()
            else -> if (args[1].equals("template", ignoreCase = true)) return amount else return emptyList()
        }
    }

    private fun handleFifthArgument(args: Array<out String>): List<String>? {
        if (args[2] != "target") {
            return emptyList()
        }
        return when (args[3]) {
            "remove", "list" -> cachedCodes.getTargetList(code = args[1])
            "add" -> null
            else -> emptyList()
        }
    }
}
