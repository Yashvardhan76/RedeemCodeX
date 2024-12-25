package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JConfig
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.command.CommandSender

class RenewSubCommand(val plugin: RedeemX): JSubCommand {
    private val codeRepo = RedeemCodeRepository(plugin)
    private val config = ConfigRepository(plugin)
    override var codeList: List<String> = emptyList()
    override val permission: String = ""
    private val service = RedeemCodeService()

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)){
            config.sendMsg(JMessage.NO_PERMISSION, placeHolder)
            return true
        }
        if (args.size < 2) {
            config.sendMsg(JMessage.Commands.Help.UNKNOWN_COMMAND, placeHolder)
            return false
        }
        val code = args[1].uppercase()
        val player = if (args.size>2){ args[2] } else ""
        placeHolder.code = args[1]

        placeHolder.player = player

        val redeemCode = codeRepo.getCode(code)

        plugin.logger.info(redeemCode?.commands.toString())

        if (redeemCode == null) {
            config.sendMsg(JMessage.Commands.Renew.NOT_FOUND, placeHolder)
            return false
        }

        if(config.getConfigValue(JConfig.Renew.CLEAR_USAGE).equals("true",ignoreCase = true)) {
            val success = codeRepo.clearUsage(redeemCode, player)
            if (!success) {
                config.sendMsg(JMessage.Commands.Renew.PLAYER_NOT_FOUND, placeHolder)
                return true
            }
        }

        if (config.getConfigValue(JConfig.Renew.RESET_DELAY).equals("true", ignoreCase = true)){
            codeRepo.clearRedeemedTime(redeemCode)
        }
        if(config.getConfigValue(JConfig.Renew.CLEAR_REWARDS).equals("true",ignoreCase = true)){
            //TODO
            return false
        }

        if(config.getConfigValue(JConfig.Renew.CLEAR_COMMANDS).equals("true",ignoreCase = true)){
            codeRepo.clearCommands(redeemCode)
        }

        if(config.getConfigValue(JConfig.Renew.RESET_EXPIRED).equals("true",ignoreCase = true)){
            codeRepo.setStoredTime(redeemCode)
        }

        if(config.getConfigValue(JConfig.Renew.REMOVE_PERMISSION_REQUIRED).equals("true",ignoreCase = true)){
            codeRepo.setPermission(redeemCode,"")
        }

        redeemCode.modified = service.getCurrentTime()
        val success = codeRepo.upsertCode(redeemCode)
        if (!success) {
            config.sendMsg(JMessage.Commands.Renew.FAILED, placeHolder)
            return true
        }
        config.sendMsg(JMessage.Commands.Renew.SUCCESS, placeHolder)
        return true
    }
}