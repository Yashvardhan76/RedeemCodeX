package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JConfig
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.utilities.JService
import org.bukkit.command.CommandSender

class RenewSubCommand(val plugin: RedeemX): JSubCommand {
    private val codeRepo = RedeemCodeRepository(plugin)
    private val config = ConfigRepository(plugin)
    override var jList: List<String> = emptyList()
    override val permission: String = ""

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)){
            config.sendMsg(JMessage.Command.NO_PERMISSION, placeHolder)
            return true
        }
        if (args.size < 2) {
            config.sendMsg(JMessage.Command.UNKNOWN_COMMAND, placeHolder)
            return false
        }
        val code = args[1].uppercase()
        val player = if (args.size>2){ args[2] } else ""
        placeHolder.code = args[1]

        val redeemCode = codeRepo.getCode(code)

        plugin.logger.info(redeemCode?.commands.toString())

        if (redeemCode == null) {
            config.sendMsg(JMessage.Code.NOT_FOUND, placeHolder)
            return false
        }

        if(config.getConfigValue(JConfig.Renew.CLEAR_USAGE).equals("true",ignoreCase = true)) {
            val success = codeRepo.clearUsage(redeemCode, player)
            if (!success) {
                config.sendMsg(JMessage.Code.Renew.PLAYER_NOT_FOUND, placeHolder)
                return true
            }
        }

        if (config.getConfigValue(JConfig.Renew.RESET_DELAY).equals("true", ignoreCase = true)){
            codeRepo.clearRedeemedTime(redeemCode)
        }
        if(config.getConfigValue(JConfig.Renew.CLEAR_REWARDS).equals("true",ignoreCase = true) && !redeemCode.sync){
            redeemCode.rewards.clear()
        }

        if(config.getConfigValue(JConfig.Renew.CLEAR_COMMANDS).equals("true",ignoreCase = true) && !redeemCode.sync){
            redeemCode.commands.clear()
        }

        if(config.getConfigValue(JConfig.Renew.RESET_EXPIRED).equals("true",ignoreCase = true)){
            redeemCode.validFrom = JService.getCurrentTime()
        }

        if(config.getConfigValue(JConfig.Renew.REMOVE_PERMISSION_REQUIRED).equals("true",ignoreCase = true) && !redeemCode.sync){
            redeemCode.permission = permission.replace("{code}", redeemCode.code)
        }

        redeemCode.modified = JService.getCurrentTime()
        val success = codeRepo.upsertCode(redeemCode)
        if (!success) {
            config.sendMsg(JMessage.Code.Renew.FAILED, placeHolder)
            return true
        }
        config.sendMsg(JMessage.Code.Renew.SUCCESS, placeHolder)
        return true
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return codeRepo.getCachedCode().toMutableList()
    }
}