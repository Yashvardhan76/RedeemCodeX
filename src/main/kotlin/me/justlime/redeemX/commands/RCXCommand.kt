package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.DeleteSubCommand
import me.justlime.redeemX.commands.subcommands.GenerateSubCommand
import me.justlime.redeemX.commands.subcommands.GenerateTemplateSubCommand
import me.justlime.redeemX.commands.subcommands.InfoSubCommand
import me.justlime.redeemX.commands.subcommands.ModifySubCommand
import me.justlime.redeemX.commands.subcommands.ModifyTemplateSubCommand
import me.justlime.redeemX.commands.subcommands.ReloadSubCommand
import me.justlime.redeemX.commands.subcommands.RenewSubCommand
import me.justlime.redeemX.data.config.yml.JMessage
import me.justlime.redeemX.data.config.yml.JPermission
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RCXCommand(private val plugin: RedeemX) : CommandExecutor {
    private val config = ConfigRepository(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val placeHolder = CodePlaceHolder(sender,args.toMutableList())
        if (args.isEmpty()) {
            config.sendMsg(JMessage.Commands.Help.HEADER, placeHolder)
            return true
        }
        if (sender.hasPermission(JPermission.Admin.USE)) {
            when (args[0].lowercase()) {
                "gen" -> if (sender.hasPermission(JPermission.Admin.GEN)) GenerateSubCommand(plugin).execute(sender, args.toMutableList())
                else config.sendMsg(JMessage.Commands.Gen.NO_PERMISSION, placeHolder)

                "gen_template" -> {
                    if (sender.hasPermission(JPermission.Admin.GEN_TEMPLATE)) GenerateTemplateSubCommand(plugin).execute(sender, args.toMutableList())
                    else config.sendMsg(JMessage.Commands.GenTemplate.NO_PERMISSION, placeHolder)
                }

                "modify",  -> if (sender.hasPermission(JPermission.Admin.MODIFY)) ModifySubCommand(plugin).execute(sender, args.toMutableList())
                else config.sendMsg(JMessage.Commands.Modify.NO_PERMISSION, placeHolder)

                "modify_template" -> if(sender.hasPermission(JPermission.Admin.MODIFY)) ModifyTemplateSubCommand(plugin).execute(sender, args.toMutableList())

                "delete", "delete_all" -> if (sender.hasPermission(JPermission.Admin.DELETE)) DeleteSubCommand(plugin).execute(sender, args.toMutableList())
                else config.sendMsg(JMessage.Commands.Delete.NO_PERMISSION, placeHolder)

                "delete_template" -> {}

                "delete_all_template" -> {}

                "info" -> if (sender.hasPermission(JPermission.Admin.INFO)) InfoSubCommand(plugin).execute(sender, args.toMutableList())
                else config.sendMsg(key = JMessage.Commands.Info.NO_PERMISSION, placeHolder)

                "renew" -> if (sender.hasPermission(JPermission.Admin.RENEW)) RenewSubCommand(plugin).execute(sender, args.toMutableList())
                else config.sendMsg(JMessage.Commands.Renew.NO_PERMISSION, placeHolder)

                "reload" -> if (sender.hasPermission(JPermission.Admin.RELOAD)) ReloadSubCommand(plugin).execute(sender, args.toMutableList())
                else config.sendMsg(JMessage.Commands.Reload.NO_PERMISSION, placeHolder)

                else -> config.sendMsg(JMessage.Commands.Help.HEADER, placeHolder)
            }
            return true
        }
        config.sendMsg("no-permission", placeHolder)
        return true
    }

}
