package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.enums.JTab
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class HelpSubCommand(plugin: RedeemX): JSubCommand {
    override var codeList: List<String> = emptyList()
    override val permission: String = ""
    val config = ConfigRepository(plugin)
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)){
            config.sendMsg(JMessage.NO_PERMISSION, placeHolder)
            return true
        }
        if(args.size<2 || args.getOrNull(1).isNullOrBlank()){
            config.sendMsg(JMessage.Commands.Help.GENERAL,placeHolder)
            return true
        }
        val command = args[1].lowercase()
        when(command){
            JTab.GeneralActions.Gen.value -> config.sendMsg(JMessage.Commands.Help.GENERATION, placeHolder)
            JTab.GeneralActions.Modify.value -> config.sendMsg(JMessage.Commands.Help.MODIFICATION, placeHolder)
            JTab.GeneralActions.Delete.value -> config.sendMsg(JMessage.Commands.Help.DELETION, placeHolder)
            JTab.GeneralActions.Renew.value -> config.sendMsg(JMessage.Commands.Help.RENEWAL, placeHolder)
            JTab.GeneralActions.Help.value -> config.sendMsg(JMessage.Commands.Help.PREVIEW, placeHolder)
            JTab.GeneralActions.Info.value -> config.sendMsg(JMessage.Commands.Help.USAGE, placeHolder)
            "permissions" -> config.sendMsg(JMessage.Commands.Help.PERMISSIONS, placeHolder)
            JTab.GeneralActions.Reload.value -> config.sendMsg(JMessage.Commands.Help.RELOAD, placeHolder)
            JTab.GeneralActions.Info.value -> config.sendMsg(JMessage.Commands.Help.INFO, placeHolder)
            else -> config.sendMsg(JMessage.Commands.Help.UNKNOWN_COMMAND, placeHolder)
        }
        return true
    }
}