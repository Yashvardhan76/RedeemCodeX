package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.DeleteSubCommand
import me.justlime.redeemX.commands.subcommands.GenerateSubCommand
import me.justlime.redeemX.commands.subcommands.HelpSubCommand
import me.justlime.redeemX.commands.subcommands.InfoSubCommand
import me.justlime.redeemX.commands.subcommands.ModifySubCommand
import me.justlime.redeemX.commands.subcommands.ModifyTemplateSubCommand
import me.justlime.redeemX.commands.subcommands.ReloadSubCommand
import me.justlime.redeemX.commands.subcommands.RenewSubCommand
import me.justlime.redeemX.commands.subcommands.UsageSubCommand
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.enums.JTab
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RCXCommand(private val plugin: RedeemX) : CommandExecutor {
    private val config = ConfigRepository(plugin)
    lateinit var placeHolder: CodePlaceHolder
    override fun onCommand(
        sender: CommandSender, command: Command, label: String, oldArgs: Array<out String>
    ): Boolean {
        placeHolder = CodePlaceHolder(sender, oldArgs.toMutableList())
        if (oldArgs.isEmpty()) {
            config.sendMsg(JMessage.Commands.Help.UNKNOWN_COMMAND, placeHolder)
            return true
        }
        val args = oldArgs.toMutableList()

        when (args[0].lowercase()) {
            JTab.GeneralActions.Gen.value -> {
                GenerateSubCommand(plugin).execute(sender, args)

            }

            JTab.GeneralActions.Modify.value -> {

                if (args.size > 1 && args[1] == JTab.Type.Code.value) ModifySubCommand(plugin).execute(sender, args)
                else if (args.size > 1 && args[1] == JTab.Type.Template.value) ModifyTemplateSubCommand(plugin).execute(sender, args)
                else config.sendMsg(JMessage.Commands.Help.UNKNOWN_COMMAND, placeHolder)
            }

            JTab.GeneralActions.Delete.value -> DeleteSubCommand(plugin).execute(sender, args)

            JTab.GeneralActions.Preview.value -> {}

            JTab.GeneralActions.Usage.value -> UsageSubCommand(plugin).execute(sender,args)

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
                else config.sendMsg(JMessage.Commands.Help.REDEEM, placeHolder)
            }

            else -> config.sendMsg(JMessage.Commands.UNKNOWN_COMMAND, placeHolder)
        }
        return true
    }
}
