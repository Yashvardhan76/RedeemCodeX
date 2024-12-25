package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class InfoSubCommand(private val plugin: RedeemX): JSubCommand {
    override var codeList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.INFO
    val config = ConfigRepository(plugin)
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)){
            config.sendMsg(JMessage.NO_PERMISSION, placeHolder)
            return true
        }
        sender.sendMessage("RedeemX Plugin Version: ${plugin.description.version}")
        return true
    }
}