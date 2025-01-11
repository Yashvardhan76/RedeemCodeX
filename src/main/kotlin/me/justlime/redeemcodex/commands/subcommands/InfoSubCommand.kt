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
import me.justlime.redeemcodex.enums.JMessage
import me.justlime.redeemcodex.enums.JPermission
import me.justlime.redeemcodex.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class InfoSubCommand(private val plugin: RedeemCodeX) : JSubCommand {
    override var jList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.INFO
    lateinit var placeHolder: CodePlaceHolder
    val config = ConfigRepository(plugin)
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)) {
            sendMessage(JMessage.Command.NO_PERMISSION)
            return true
        }
        sendMessage("${JMessage.Command.INFO} - ${plugin.description.version}")
        return true
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return mutableListOf()
    }

    override fun sendMessage(key: String): Boolean {
        placeHolder.sentMessage = config.getMessage(key, placeHolder)
        config.sendMsg(key, placeHolder)
        return true
    }
}