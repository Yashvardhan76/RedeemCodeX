package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.yml.JMessage
import me.justlime.redeemX.state.RedeemCodeState

class DeleteSubCommand(private val plugin: RedeemX) : JSubCommand {
    private val config = plugin.configFile
    private val stateManager = plugin.stateManager

    override fun execute(state: RedeemCodeState): Boolean {
       val args = state.args


        if (args.size < 2 && args[0].equals("delete", ignoreCase = true)) {
            config.sendMsg(JMessage.Commands.Delete.INVALID_SYNTAX, state)
            return false
        }
        if (args.size < 2 && args[0].equals("delete_all", ignoreCase = true)) {
            config.sendMsg(JMessage.Commands.DeleteAll.INVALID_SYNTAX, state)
            return false
        }
        when (args[0].lowercase()) {
            "delete" -> {
                state.inputCode = args[1]
                val redeemCode = plugin.redeemCodeDB.get(state.inputCode)

                if (redeemCode == null) {
                    config.sendMsg(JMessage.Commands.Delete.NOT_FOUND, state)
                    return false
                }
                val success = plugin.redeemCodeDB.deleteByCode(state.inputCode)
                if (!success) {
                    config.sendMsg(JMessage.Commands.Delete.FAILED, state)
                    return false
                }
                config.sendMsg(JMessage.Commands.Delete.SUCCESS, state)
                return true
            }

            "delete_all" -> {
                if (args[1] != "CONFIRM") {
                    config.sendMsg(JMessage.Commands.DeleteAll.CONFIRMATION, state)
                    return false
                }
                val success = plugin.redeemCodeDB.deleteEntireCodes()
                if (!success) {
                    config.sendMsg(JMessage.Commands.DeleteAll.FAILED, state)
                    return false
                }
                config.sendMsg(JMessage.Commands.DeleteAll.SUCCESS, state)
                return true
            }

            else -> config.sendMsg(JMessage.Commands.Delete.INVALID_SYNTAX, state)
        }
        return false
    }
}

