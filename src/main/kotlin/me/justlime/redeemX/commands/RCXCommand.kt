package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.DeleteSubCommand
import me.justlime.redeemX.commands.subcommands.GenerateSubCommand
import me.justlime.redeemX.commands.subcommands.GenerateTemplateSubCommand
import me.justlime.redeemX.commands.subcommands.InfoSubCommand
import me.justlime.redeemX.commands.subcommands.ModifySubCommand
import me.justlime.redeemX.commands.subcommands.ReloadSubCommand
import me.justlime.redeemX.commands.subcommands.RenewSubCommand
import me.justlime.redeemX.data.config.ConfigManager
import me.justlime.redeemX.data.config.yml.JMessage
import me.justlime.redeemX.data.config.yml.JPermission
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RCXCommand(private val plugin: RedeemX) : CommandExecutor {
    private val config = ConfigManager(plugin)
    private val stateManager = plugin.stateManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val state = stateManager.getState(sender)
        if (args.isEmpty()) {
            config.sendMsg(JMessage.Commands.Help.HEADER, state)
            return true
        }
        state.args = args.toMutableList()
        if (sender.hasPermission(JPermission.Admin.USE)) {
            when (args[0].lowercase()) {
                "gen" -> if (sender.hasPermission(JPermission.Admin.GEN)) GenerateSubCommand(plugin).execute(sender, args)
                else config.sendMsg(JMessage.Commands.Gen.NO_PERMISSION, state)

                "gen_template" -> {
                    if (sender.hasPermission(JPermission.Admin.GEN_TEMPLATE)) GenerateTemplateSubCommand(plugin).execute(state)
                    else config.sendMsg(JMessage.Commands.Gen_Template.NO_PERMISSION, state)
                }

                "modify", "modify_template" -> if (sender.hasPermission(JPermission.Admin.MODIFY)) ModifySubCommand(plugin).execute(state)
                else config.sendMsg(JMessage.Commands.Modify.NO_PERMISSION, state)

                "delete", "delete_all" -> if (sender.hasPermission(JPermission.Admin.DELETE)) DeleteSubCommand(plugin).execute(state)
                else config.sendMsg(JMessage.Commands.Delete.NO_PERMISSION, state)

                "delete_template" -> {}

                "delete_all_template" -> {}

                "info" -> if (sender.hasPermission(JPermission.Admin.INFO)) InfoSubCommand(plugin).execute(sender)
                else config.sendMsg(key = JMessage.Commands.Info.NO_PERMISSION, state)

                "renew" -> if (sender.hasPermission(JPermission.Admin.RENEW)) RenewSubCommand(plugin).execute(sender, args)
                else config.sendMsg(JMessage.Commands.Renew.NO_PERMISSION, state)

                "reload" -> if (sender.hasPermission(JPermission.Admin.RELOAD)) ReloadSubCommand(plugin).execute(state)
                else config.sendMsg(JMessage.Commands.Reload.NO_PERMISSION, state)

                else -> sender.sendMessage(config.getString(JMessage.Commands.UNKNOWN_COMMAND))
            }
            return true
        }
        config.sendMsg("no-permission", state)
        return true
    }

}
