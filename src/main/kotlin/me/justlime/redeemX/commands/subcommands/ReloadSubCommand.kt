package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.JMessage
import me.justlime.redeemX.config.JPermission
import me.justlime.redeemX.state.RedeemCodeState

class ReloadSubCommand(val plugin: RedeemX) : JSubCommand {
    private val config = plugin.configFile
    private val db = plugin.redeemCodeDB

    override fun execute(state: RedeemCodeState): Boolean {
        val sender = state.sender
        if (!sender.hasPermission(JPermission.Admin.RELOAD)) {
            config.dm(JMessage.NO_PERMISSION, state)
            return false
        }
        try {
            config.reloadAllConfigs()
            db.init()
            config.dm(JMessage.Commands.Reload.SUCCESS, state)
            return true

        } catch (e: Exception) {
            config.dm(JMessage.Commands.Reload.FAILED, state)
            return false
        }
    }
}
