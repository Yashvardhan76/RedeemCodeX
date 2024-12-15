package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class ReloadSubCommand(val plugin: RedeemX) : JSubCommand {
    private val db = plugin.redeemCodeDB

    override fun execute(sender: CommandSender,args: MutableList<String>): Boolean {
        val config = ConfigRepository(plugin)
        val placeHolder = CodePlaceHolder(sender)
        if (!sender.hasPermission(JPermission.Admin.RELOAD)) {
            config.sendMsg(JMessage.NO_PERMISSION, placeHolder)
            return false
        }
        try {
            config.reloadConfig()
            db.fetch()
            config.sendMsg(JMessage.Commands.Reload.SUCCESS, placeHolder)
            return true

        } catch (e: Exception) {
            config.sendMsg(JMessage.Commands.Reload.FAILED, placeHolder)
            return false
        }
    }
}
