package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.yml.JConfig
import me.justlime.redeemX.data.config.yml.JMessage
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.CommandSender
import java.sql.Timestamp

class RenewSubCommand(val plugin: RedeemX): JSubCommand {
    private val codeRepo = RedeemCodeRepository(plugin)
    private val config = ConfigRepository(plugin)

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender)
        if (args.size < 2) {
            config.sendMsg(JMessage.Commands.Renew.INVALID_SYNTAX, placeHolder)
            return false
        }
        val code = args[1]
        placeHolder.code = args[1]

        val redeemCode = codeRepo.getCode(code)

        if (redeemCode == null) {
            config.sendMsg(JMessage.Commands.Renew.NOT_FOUND, placeHolder)
            return false
        }

        if(config.getConfigValue(JConfig.Renew.CLEAR_USAGE).equals("true",ignoreCase = true)) {
            redeemCode.usage.clear()
        }

        //TODO ADD DELAY
        if (config.getConfigValue(JConfig.Renew.RESET_DELAY).equals("true", ignoreCase = true)){
            //TODO
            return false
        }
        if(config.getConfigValue(JConfig.Renew.CLEAR_REWARDS).equals("true",ignoreCase = true)){
            //TODO
            return false
        }

        if(config.getConfigValue(JConfig.Renew.CLEAR_COMMANDS).equals("true",ignoreCase = true)){
            redeemCode.commands.clear()
        }

        if(config.getConfigValue(JConfig.Renew.RESET_EXPIRED).equals("true",ignoreCase = true)){
            redeemCode.storedTime = Timestamp.valueOf(plugin.service.currentTime)
        }

        if(config.getConfigValue(JConfig.Renew.REMOVE_PERMISSION_REQUIRED).equals("true",ignoreCase = true)){
            redeemCode.permission = null
        }


        val success = codeRepo.upsertCode(redeemCode)
        if (!success) {
            config.sendMsg(JMessage.Commands.Renew.FAILED, placeHolder)
            return true
        }
        config.sendMsg(JMessage.Commands.Renew.SUCCESS, placeHolder)
        return true
    }
}