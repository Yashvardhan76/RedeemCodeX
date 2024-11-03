package me.justlime.redeemX.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter


class TabCompleterList : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): MutableList<String>? {
        val completions: MutableList<String> = mutableListOf()
        val onlinePlayers = Bukkit.getOnlinePlayers()
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
                "modify" -> completions.add("codeId")
                "delete" -> completions.add("codeId")
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
                    completions.add("max_redeems_per_player")
                    completions.add("expire_time")
                    completions.add("permission")
                    completions.add("changeCode")
                    completions.add("specificPlayerId")
                    completions.add("secureCode")
                    completions.add("AddCommand")
                    completions.add("deleteCommand")
                    completions.add("deleteAllCommands")
                    return completions
                }
            }

            4 -> when (args[2]) {
                "cmd" -> {}
                "template" -> {}

                "enabled" -> {
                    completions.add("true")
                    completions.add("false")
                }

                "max_redeems" -> {}
                "max_per_player" -> {}
                "max_redeems_per_player" -> {
                    completions.add("TODO")
                }

                "expire_time" -> {
                    completions.add("7d")
                }

                "permission" -> {
                    completions.add("true")
                    completions.add("false")
                    completions.add("CUSTOM")
                }

                "changeCode" -> {
                    completions.add("SIZE")
                    completions.add("CUSTOM")
                }

                "secureCode" -> {}
                "AddCommand" -> {}
                "deleteCommand" -> {
                    completions.add("TODO")
                }

                "specificPlayerId" -> {
                    return null
                }
            }
        }

        return completions
    }

}