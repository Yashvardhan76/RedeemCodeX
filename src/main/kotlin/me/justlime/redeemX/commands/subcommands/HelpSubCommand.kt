package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.enums.JTab
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class HelpSubCommand(plugin: RedeemX): JSubCommand {
    override var jList: List<String> = emptyList()
    override val permission: String = ""
    val config = ConfigRepository(plugin)
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)){
            config.sendMsg(JMessage.Command.NO_PERMISSION, placeHolder)
            return true
        }
        if(args.size<2 || args.getOrNull(1).isNullOrBlank()){
            config.sendMsg(JMessage.Command.Help.GENERAL,placeHolder)
            return true
        }
        val command = args[1].lowercase()
        when(command){
            JTab.GeneralActions.Gen.value -> config.sendMsg(JMessage.Command.Help.GENERATION, placeHolder)
            JTab.GeneralActions.Modify.value -> config.sendMsg(JMessage.Command.Help.MODIFICATION, placeHolder)
            JTab.GeneralActions.Delete.value -> config.sendMsg(JMessage.Command.Help.DELETION, placeHolder)
            JTab.GeneralActions.Renew.value -> config.sendMsg(JMessage.Command.Help.RENEWAL, placeHolder)
            JTab.GeneralActions.Help.value -> config.sendMsg(JMessage.Command.Help.PREVIEW, placeHolder)
            JTab.GeneralActions.Info.value -> config.sendMsg(JMessage.Command.Help.USAGE, placeHolder)
            "permissions" -> config.sendMsg(JMessage.Command.Help.PERMISSIONS, placeHolder)
            JTab.GeneralActions.Reload.value -> config.sendMsg(JMessage.Command.Help.RELOAD, placeHolder)
            JTab.GeneralActions.Info.value -> config.sendMsg(JMessage.Command.INFO, placeHolder)
            else -> config.sendMsg(JMessage.Command.UNKNOWN_COMMAND, placeHolder)
        }
        return true
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        TODO("Not yet implemented")
    }
}