/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *  This file handles the core logic for redeeming codes and managing associated data.
 *
 */


package me.justlime.redeemcodex.commands.subcommands

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.commands.JSubCommand
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.enums.JFiles
import me.justlime.redeemcodex.enums.JMessage
import me.justlime.redeemcodex.enums.JPermission
import me.justlime.redeemcodex.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class ReloadSubCommand(val plugin: RedeemCodeX) : JSubCommand {
    lateinit var placeHolder: CodePlaceHolder
    val config = ConfigRepository(plugin)
    override var jList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.RELOAD

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        placeHolder = CodePlaceHolder(sender)
        if (!sender.hasPermission(JPermission.Admin.RELOAD)) {
            sendMessage(JMessage.Command.NO_PERMISSION)
            return false
        }
        try {
            config.reloadConfig(JFiles.CONFIG)
            config.reloadConfig(JFiles.MESSAGES)
            config.reloadConfig(JFiles.TEMPLATE)
//            CommandManager(plugin).tabCompleterList.fetched()
            sendMessage(JMessage.Command.Reload.SUCCESS)
            return true

        } catch (e: Exception) {
            sendMessage(JMessage.Command.Reload.FAILED)
            return false
        }
    }

    override fun sendMessage(key: String): Boolean {
        placeHolder.sentMessage = config.getMessage(key, placeHolder)
        config.sendMsg(key, placeHolder)
        return true
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return mutableListOf()
    }
}
