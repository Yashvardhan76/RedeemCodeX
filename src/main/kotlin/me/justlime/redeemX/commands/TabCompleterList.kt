package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.enums.JTab
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TabCompleterList(val plugin: RedeemX) : TabCompleter {
    // Predefined common completions for each command type
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    private val deleteOptions = listOf(JTab.Delete.All.value)
    private val typeOptions = JTab.Type.entries.map { it.value }
    private val modifyOptions = JTab.Modify.entries.map { it.value }
    private val templateOptions = JTab.Template.entries.map { it.value }
    private val genOptions = JTab.Generate.entries.filter { it != JTab.Generate.Amount }.map { it.value }
    private var cachedCodes = codeRepo.getCachedCode()
    private var cachedTemplate = config.getAllTemplates().map { it.name }
    private var cachedTargetList = codeRepo.getCachedTargetList()
    private var cachedUsageList = codeRepo.getCachedUsageList()
    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): MutableList<String>? {
        val completions: MutableList<String> = mutableListOf()
        val generalOptions: MutableList<String> = mutableListOf()
        if (sender.hasPermission(JPermission.Admin.GEN)) generalOptions.add(JTab.GeneralActions.Gen.value)
        if (sender.hasPermission(JPermission.Admin.MODIFY)) generalOptions.add(JTab.GeneralActions.Modify.value)
        if (sender.hasPermission(JPermission.Admin.DELETE)) generalOptions.add(JTab.GeneralActions.Delete.value)
        if (sender.hasPermission(JPermission.Admin.RENEW)) generalOptions.add(JTab.GeneralActions.Renew.value)
        if (sender.hasPermission(JPermission.Admin.INFO)) generalOptions.add(JTab.GeneralActions.Info.value)
        if (sender.hasPermission(JPermission.Admin.RELOAD)) generalOptions.add(JTab.GeneralActions.Reload.value)
        // Handle argument completions based on argument size

        when (args.size) {
            1 -> {
                completions.addAll(generalOptions)
            }

            2 -> {
                when (args[0]) {
                    JTab.GeneralActions.Gen.value -> if (sender.hasPermission(JPermission.Admin.GEN)) completions.addAll(typeOptions)
                    JTab.GeneralActions.Modify.value -> if (sender.hasPermission(JPermission.Admin.MODIFY)) completions.addAll(typeOptions)
                    JTab.GeneralActions.Delete.value -> completions.addAll(typeOptions)
                    JTab.GeneralActions.Renew.value -> completions.addAll(cachedUsageList[args[1]]?.keys ?: listOf())
                    JTab.GeneralActions.Help.value -> {
                        completions.addAll(generalOptions)
                        completions.add("permissions")
                    }
                    JTab.GeneralActions.Usage.value -> if(sender.hasPermission(JPermission.Admin.USAGE)) completions.addAll(typeOptions)

                }
            }

            3 -> {
                if (args[1] == JTab.Type.Code.value) when (args[0]) {
                    JTab.GeneralActions.Gen.value -> if (sender.hasPermission(JPermission.Admin.GEN)) completions.addAll(genOptions)
                    JTab.GeneralActions.Modify.value -> if (sender.hasPermission(JPermission.Admin.MODIFY)) completions.addAll(cachedCodes)

                    JTab.GeneralActions.Delete.value -> if (sender.hasPermission(JPermission.Admin.DELETE)) {
                        completions.addAll(cachedCodes)
                        if (cachedCodes.isNotEmpty()) completions.addAll(deleteOptions)
                    }
                    JTab.GeneralActions.Usage.value -> if(sender.hasPermission(JPermission.Admin.USAGE)) completions.addAll(cachedCodes)
                }
                if (args[1] == JTab.Type.Template.value) when (args[0]) {
                    JTab.GeneralActions.Modify.value -> if (sender.hasPermission(JPermission.Admin.MODIFY)) completions.addAll(cachedTemplate)
                    JTab.GeneralActions.Delete.value -> if (sender.hasPermission(JPermission.Admin.DELETE)) completions.addAll(cachedTemplate)
                    JTab.GeneralActions.Usage.value -> if(sender.hasPermission(JPermission.Admin.USAGE)) completions.addAll(cachedTemplate)
                }
            }

            4 -> {

                when (args[0]) {
                    JTab.GeneralActions.Gen.value -> if (args[1] == JTab.Type.Code.value) completions.addAll(cachedTemplate)
                    JTab.GeneralActions.Modify.value -> {
                        when (args[1]) {
                            JTab.Type.Code.value -> if (sender.hasPermission(JPermission.Admin.MODIFY)) completions.addAll(modifyOptions)
                            JTab.Type.Template.value -> if (sender.hasPermission(JPermission.Admin.MODIFY)) completions.addAll(templateOptions)
                        }

                    }
                }

            }

            5 -> {
                if (args[0] == JTab.GeneralActions.Gen.value && args[1] == JTab.Type.Code.value && args[2].toIntOrNull() != null) completions.add(JTab.Generate.Amount.value)
            }

            6 -> {
                when (args[2]) {
                    JTab.Modify.RemoveTarget.value -> if (sender.hasPermission(JPermission.Admin.MODIFY)) {
                        val list = cachedTargetList[args[1]] ?: mutableListOf()
                        completions.addAll(list)
                    }

                    JTab.Modify.SetTarget.value, JTab.Modify.AddTarget.value -> if (sender.hasPermission(JPermission.Admin.MODIFY)) {
                        return null
                    }
                }
            }
            //For Endless
            else -> {
                if (sender.hasPermission(JPermission.Admin.MODIFY)) {
                    if (args[2].contains(JTab.Modify.SetTarget.value) || args[2].contains(JTab.Modify.AddTarget.value)) {
                        return null
                    }
                }
            }
        }

        // Filter and return completions that match the current input (case-insensitive)
        return completions.filter { it ->
            it.contains(args.lastOrNull() ?: "", ignoreCase = true)
        }.sortedBy { it.lowercase() }.toMutableList()
    }

    fun fetched() {
        codeRepo.fetch()
    }
}
