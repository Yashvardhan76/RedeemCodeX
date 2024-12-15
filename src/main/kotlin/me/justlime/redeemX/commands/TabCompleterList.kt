package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.Tab
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TabCompleterList(val plugin: RedeemX) : TabCompleter {
    // Predefined common completions for each command type
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)

    private val commonCompletions = Tab.GeneralActions.entries.map { it.value }

    private val modifyOptions = Tab.Modify.entries.map { it.value }
    private val modifyTemplateOptions = Tab.Template.entries.map { it.value }
    private val amount = listOf(Tab.Generate.Amount.value)
    private val genSubcommands = Tab.Generate.entries.filter { it != Tab.Generate.Amount }.map { it.value }
    private var cachedCodes = codeRepo.getCachedCode()
    private var cachedTemplate = config.getEntireTemplates().map { it.name }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): MutableList<String>? {
        val completions: MutableList<String> = mutableListOf()

        // Handle argument completions based on argument size
        when (args.size) {
            1 -> {
                completions.addAll(commonCompletions)
            }

            2 -> {
                when (args[0]) {
                    Tab.GeneralActions.Gen.value -> completions.addAll(genSubcommands)
                    Tab.GeneralActions.Modify.value -> completions.addAll(cachedCodes)
                    Tab.GeneralActions.ModifyTemplate.value -> completions.addAll(cachedCodes)
                    Tab.GeneralActions.Delete.value -> completions.addAll(cachedCodes)
                }
            }

            3 -> {
                when (args[0]) {
                    Tab.GeneralActions.Modify.value -> completions.addAll(modifyOptions)
                    Tab.GeneralActions.ModifyTemplate.value -> completions.addAll(modifyTemplateOptions)
                }
                when (args[1]) {
                    Tab.Generate.Template.value -> completions.addAll(cachedTemplate)
                }
            }

        }

        // Filter and return completions that match the current input (case-insensitive)
        return completions.filter { it.startsWith(args.lastOrNull() ?: "", ignoreCase = true) }.sortedBy { it.lowercase() }.toMutableList()
    }
}
