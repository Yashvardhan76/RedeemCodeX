/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 *
 */


package me.justlime.redeemcodex.commands.subcommands

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.commands.JSubCommand
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.data.repository.RedeemCodeRepository
import me.justlime.redeemcodex.enums.JConfig
import me.justlime.redeemcodex.enums.JMessage
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.command.CommandSender

class RenewSubCommand(val plugin: RedeemCodeX): JSubCommand {
    lateinit var placeHolder: CodePlaceHolder
    private val codeRepo = RedeemCodeRepository(plugin)
    private val config = ConfigRepository(plugin)
    override var jList: List<String> = emptyList()
    override val permission: String = ""

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)){
            sendMessage(JMessage.Command.NO_PERMISSION)
            return true
        }
        if (args.size < 2) {
            sendMessage(JMessage.Command.UNKNOWN_COMMAND)
            return false
        }
        val code = args[1].uppercase()
        val player = if (args.size>2){ args[2] } else ""
        placeHolder.code = args[1]

        val redeemCode = codeRepo.getCode(code)

        plugin.logger.info(redeemCode?.commands.toString())

        if (redeemCode == null) {
            sendMessage(JMessage.Code.NOT_FOUND)
            return false
        }

        if(config.getConfigValue(JConfig.Renew.CLEAR_USAGE).equals("true",ignoreCase = true)) {
            val success = codeRepo.clearUsage(redeemCode, player)
            if (!success) {
                sendMessage(JMessage.Code.Renew.PLAYER_NOT_FOUND)
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
            sendMessage(JMessage.Code.Renew.FAILED)
            return true
        }
        sendMessage(JMessage.Code.Renew.SUCCESS)
        return true
    }

    override fun sendMessage(key: String): Boolean {
        placeHolder.sentMessage = config.getMessage(key, placeHolder)
        config.sendMsg(key, placeHolder)
        return true
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return codeRepo.getCachedCode().toMutableList()
    }
}