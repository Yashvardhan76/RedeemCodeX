package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class DeleteSubCommand(private val plugin: RedeemX) : JSubCommand {
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    override var codeList: List<String> = emptyList()
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender)

        if (args.size < 2 && args[0].equals("delete", ignoreCase = true)) {
            config.sendMsg(JMessage.Commands.Delete.INVALID_SYNTAX, placeHolder)
            return false
        }
        if (args.size < 2 && args[0].equals("delete_all", ignoreCase = true)) {
            config.sendMsg(JMessage.Commands.DeleteAll.INVALID_SYNTAX, placeHolder)
            return false
        }
        when (args[0].lowercase()) {
            "delete" -> {
                val code = args[1]
                placeHolder.code = code
                val redeemCode = codeRepo.getCode(code)

                if (redeemCode == null) {
                    config.sendMsg(JMessage.Commands.Delete.NOT_FOUND, placeHolder)
                    return false
                }
                val success = codeRepo.deleteCode(code)
                if (!success) {
                    config.sendMsg(JMessage.Commands.Delete.FAILED, placeHolder)
                    return false
                }
                config.sendMsg(JMessage.Commands.Delete.SUCCESS, placeHolder)
                return true
            }

            "delete_all" -> {

                if (args[1] != "CONFIRM") {
                    config.sendMsg(JMessage.Commands.DeleteAll.CONFIRMATION, placeHolder)
                    return false
                }
                val success = codeRepo.deleteEntireCodes()
                if (!success) {
                    config.sendMsg(JMessage.Commands.DeleteAll.FAILED, placeHolder)
                    return false
                }
                config.sendMsg(JMessage.Commands.DeleteAll.SUCCESS, placeHolder)
                return true
            }

            else -> config.sendMsg(JMessage.Commands.UNKNOWN_COMMAND, placeHolder)
        }
        return false
    }
}

