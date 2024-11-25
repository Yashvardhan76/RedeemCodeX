package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.JMessage
import org.bukkit.command.CommandSender

class DeleteSubCommand(private val plugin: RedeemX) : JSubCommand {
    private val config = plugin.configFile
    private val stateManager = plugin.stateManager

    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        val state = stateManager.createState(sender)
        if (args.size < 2 && args[0].equals("delete", ignoreCase = true)) {
            config.dm(JMessage.Commands.Delete.INVALID_SYNTAX, state)
            return false
        }
        if (args.size < 2 && args[0].equals("delete_all", ignoreCase = true)) {
            config.dm(JMessage.Commands.DeleteAll.INVALID_SYNTAX, state)
            return false
        }
        when (args[0].lowercase()) {
            "delete" -> {
                state.inputCode = args[1]
                val redeemCode = plugin.redeemCodeDB.get(state.inputCode)

                if (redeemCode == null) {
                    config.dm(JMessage.Commands.Delete.NOT_FOUND, state)
                    return false
                }
                val success = plugin.redeemCodeDB.deleteByCode(state.inputCode)
                if (!success) {
                    config.dm(JMessage.Commands.Delete.FAILED, state)
                    return false
                }
                config.dm(JMessage.Commands.Delete.SUCCESS, state)
                return true
            }

            "delete_all" -> {
                if (args[1] != "CONFIRM") {
                    config.dm(JMessage.Commands.DeleteAll.CONFIRMATION, state)
                    return false
                }
                val success = plugin.redeemCodeDB.deleteAll()
                if (!success) {
                    config.dm(JMessage.Commands.DeleteAll.FAILED, state)
                    return false
                }
                config.dm(JMessage.Commands.DeleteAll.SUCCESS, state)
                return true
            }

            else -> config.dm(JMessage.Commands.Delete.INVALID_SYNTAX, state)
        }
        return false
    }
}

