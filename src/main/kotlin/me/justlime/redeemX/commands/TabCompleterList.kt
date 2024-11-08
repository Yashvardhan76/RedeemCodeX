package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter


class TabCompleterList(private val plugin: RedeemX) : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): MutableList<String>? {
        var completions: MutableList<String> = mutableListOf()
        Bukkit.getOnlinePlayers()
            .filter { player -> player.name.startsWith(args[0], true) } // true for case-insensitive matching

        // Add filtered player names to completions
        // Collect names of matching players


        when (args.size) {
            1 -> {
                completions.add("gen")
                completions.add("modify")
                completions.add("delete")
                completions.add("delete_all")
            }

            2 -> when (args[0]) {
                "gen" -> {
                    completions.add("CUSTOM")
                    completions.add("SIZE")
                }

                "modify" -> completions = tabCodes().toMutableList()
                "delete" -> completions = tabCodes().toMutableList()
            }

            3 -> when (args[0]) {
                "gen" -> {
                    completions.add("cmd")
                    completions.add("template")
                }

                "modify" -> {
                    completions.add("enabled")
                    completions.add("max_redeems")
                    completions.add("max_per_player")
                    completions.add("duration")
                    completions.add("permission")
//                    completions.add("change_code") TODO
                    completions.add("set_target")
                    completions.add("set_pin")
                    completions.add("command")
                    completions.add("list")
//                    completions.add("rewards") TODO
                    return completions
                }
            }

            4 -> when (args[2]) {
                "cmd" -> {}
                "template" -> {}
                "duration" -> {
                    completions.add("add")
                    completions.add("set")
                    completions.add("unset")
                }

                "enabled" -> {
                    completions.add("true")
                    completions.add("false")
                }

                "max_redeems" -> {}
                "max_per_player" -> {}
                "max_redeems_per_player" -> {}
                "permission" -> {
                    completions.add("true")
                    completions.add("false")
                    completions.add("CUSTOM")
                }

//                "change_code" -> {
//                    completions.add("SIZE")
//                    completions.add("CUSTOM")
//                }

                "set_pin" -> {
                    completions.add("-1")
                }

                "command" -> {
                    completions.add("add")
                    completions.add("set")
                    completions.add("list")
                    completions.add("preview")
                }

                "rewards" -> {
                    TODO()
                }

                "set_target" -> {
                    return null
                }
            }
        }
        return completions
            .filter { it.startsWith(args.lastOrNull() ?: "", ignoreCase = true) }
            .sortedBy { it.lowercase() }
            .toMutableList()
    }

    private fun tabCodes(): Array<String> {
        val completion: Array<String> = plugin.redeemCodeDao.getAllCodes().map { it.code }.toTypedArray()
        return completion
    }
}

