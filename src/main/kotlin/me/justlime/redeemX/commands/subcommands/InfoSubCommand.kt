package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class InfoSubCommand(private val plugin: RedeemX): JSubCommand {
    override var jList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.INFO
    val config = ConfigRepository(plugin)
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)){
            config.sendMsg(JMessage.Command.NO_PERMISSION, placeHolder)
            return true
        }
        sender.sendMessage("${JMessage.Command.INFO} - ${plugin.description.version}")
        return true
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return mutableListOf()
    }
}