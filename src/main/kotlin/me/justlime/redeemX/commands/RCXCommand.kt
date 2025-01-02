package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.*
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.enums.JTab
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class RCXCommand(private val plugin: RedeemX) : CommandExecutor, TabExecutor {
    private val config = ConfigRepository(plugin)
    lateinit var placeHolder: CodePlaceHolder
    override fun onCommand(
        sender: CommandSender, command: Command, label: String, oldArgs: Array<out String>
    ): Boolean {
        placeHolder = CodePlaceHolder(sender, oldArgs.toMutableList())
        if (oldArgs.isEmpty()) {
            config.sendMsg(JMessage.Command.UNKNOWN_COMMAND, placeHolder)
            return true
        }
        val args = oldArgs.toMutableList()
        val permissionList = listOf(
            JPermission.Admin.GEN,
            JPermission.Admin.MODIFY,
            JPermission.Admin.DELETE,
            JPermission.Admin.PREVIEW,
            JPermission.Admin.RENEW,
            JPermission.Admin.RELOAD
        )
        if (!permissionList.any { sender.hasPermission(it) }) {
            config.sendMsg(JMessage.Command.NO_PERMISSION, placeHolder)
            return true
        }

        when (args[0].lowercase()) {
            JTab.GeneralActions.Gen.value -> GenerateSubCommand(plugin).execute(sender, args)

            JTab.GeneralActions.Modify.value -> ModifySubCommand(plugin).execute(sender, args)

            JTab.GeneralActions.Delete.value -> DeleteSubCommand(plugin).execute(sender, args)

            JTab.GeneralActions.Preview.value -> PreviewSubCommand(plugin).execute(sender, args)

            JTab.GeneralActions.Usage.value -> UsageSubCommand(plugin).execute(sender, args)

            JTab.GeneralActions.Info.value -> InfoSubCommand(plugin).execute(sender, args)

            JTab.GeneralActions.Renew.value -> RenewSubCommand(plugin).execute(sender, args)

            JTab.GeneralActions.Reload.value -> ReloadSubCommand(plugin).execute(sender, args)

            JTab.GeneralActions.Help.value -> {
                if (sender.hasPermission(JPermission.Admin.GEN) || sender.hasPermission(JPermission.Admin.DELETE) || sender.hasPermission(JPermission.Admin.MODIFY) || sender.hasPermission(
                        JPermission.Admin.INFO
                    ) || sender.hasPermission(JPermission.Admin.PREVIEW) || sender.hasPermission(JPermission.Admin.RENEW) || sender.hasPermission(
                        JPermission.Admin.RELOAD
                    )
                ) HelpSubCommand(plugin).execute(sender, args)
                else config.sendMsg(JMessage.Command.Help.REDEEM, placeHolder)
            }

            else -> config.sendMsg(JMessage.Command.UNKNOWN_COMMAND, placeHolder)
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>?): MutableList<String>? {
        val completions: MutableList<String> = mutableListOf()
        val generalOptions: MutableList<String> = mutableListOf()


        if (sender.hasPermission(JPermission.Admin.GEN)) generalOptions.add(JTab.GeneralActions.Gen.value)
        if (sender.hasPermission(JPermission.Admin.MODIFY)) generalOptions.add(JTab.GeneralActions.Modify.value)
        if (sender.hasPermission(JPermission.Admin.DELETE)) generalOptions.add(JTab.GeneralActions.Delete.value)
        if (sender.hasPermission(JPermission.Admin.RENEW)) generalOptions.add(JTab.GeneralActions.Renew.value)
        if (sender.hasPermission(JPermission.Admin.INFO)) generalOptions.add(JTab.GeneralActions.Info.value)
        if (sender.hasPermission(JPermission.Admin.RELOAD)) generalOptions.add(JTab.GeneralActions.Reload.value)
        if (sender.hasPermission(JPermission.Admin.USAGE)) generalOptions.add(JTab.GeneralActions.Usage.value)
        if (sender.hasPermission(JPermission.Admin.PREVIEW)) generalOptions.add(JTab.GeneralActions.Preview.value)
        if (args == null) return mutableListOf()
        if (args.isEmpty()) return mutableListOf()

        if (args.size == 1) completions.addAll(generalOptions)
        (if (args.size >= 2) {
            when (args[0]) {
                JTab.GeneralActions.Gen.value -> if (sender.hasPermission(JPermission.Admin.GEN)) GenerateSubCommand(plugin).tabCompleter(sender, args.toMutableList()) else mutableListOf()
                JTab.GeneralActions.Modify.value -> if (sender.hasPermission(JPermission.Admin.MODIFY)) ModifySubCommand(plugin).tabCompleter(sender, args.toMutableList()) else mutableListOf()
                JTab.GeneralActions.Delete.value -> if (sender.hasPermission(JPermission.Admin.DELETE)) DeleteSubCommand(plugin).tabCompleter(sender, args.toMutableList()) else mutableListOf()
                JTab.GeneralActions.Preview.value -> if (sender.hasPermission(JPermission.Admin.PREVIEW)) PreviewSubCommand(plugin).tabCompleter(sender, args.toMutableList()) else mutableListOf()
                JTab.GeneralActions.Usage.value -> if (sender.hasPermission(JPermission.Admin.USAGE)) UsageSubCommand(plugin).tabCompleter(sender, args.toMutableList()) else mutableListOf()
                JTab.GeneralActions.Renew.value -> if (sender.hasPermission(JPermission.Admin.RENEW)) RenewSubCommand(plugin).tabCompleter(sender, args.toMutableList()) else mutableListOf()
                JTab.GeneralActions.Help.value -> HelpSubCommand(plugin).tabCompleter(sender, args.toMutableList())
                JTab.GeneralActions.Info.value -> if (sender.hasPermission(JPermission.Admin.INFO)) InfoSubCommand(plugin).tabCompleter(sender, args.toMutableList()) else mutableListOf()
                JTab.GeneralActions.Reload.value -> if (sender.hasPermission(JPermission.Admin.RELOAD)) ReloadSubCommand(plugin).tabCompleter(sender, args.toMutableList()) else mutableListOf()
                 else -> mutableListOf()
            }
        } else mutableListOf()).let { completions.addAll(it ?: return null) }

        return completions.filter {
            it.contains(args.lastOrNull() ?: "", ignoreCase = true)
        }.sortedBy { it.lowercase() }.toMutableList()
    }
}
