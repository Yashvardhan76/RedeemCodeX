package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.JSubCommand
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.enums.JFiles
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class ReloadSubCommand(val plugin: RedeemX) : JSubCommand {
    lateinit var placeHolder: CodePlaceHolder
    val config = ConfigRepository(plugin)
    override var jList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.RELOAD

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        placeHolder = CodePlaceHolder(sender)
        if (!sender.hasPermission(JPermission.Admin.RELOAD)) {
            sendMessage(JMessage.Command.NO_PERMISSION)
            return false
        }
        try {
            config.reloadConfig(JFiles.CONFIG)
            config.reloadConfig(JFiles.MESSAGES)
            config.reloadConfig(JFiles.TEMPLATE)
//            CommandManager(plugin).tabCompleterList.fetched()
            sendMessage(JMessage.Command.Reload.SUCCESS)
            return true

        } catch (e: Exception) {
            sendMessage(JMessage.Command.Reload.FAILED)
            return false
        }
    }

    override fun sendMessage(key: String): Boolean {
        placeHolder.sentMessage = config.getMessage(key, placeHolder)
        config.sendMsg(key, placeHolder)
        return true
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return mutableListOf()
    }
}
