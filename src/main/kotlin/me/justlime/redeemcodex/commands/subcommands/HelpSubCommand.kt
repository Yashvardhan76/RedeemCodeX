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
import me.justlime.redeemcodex.enums.JTab
import me.justlime.redeemcodex.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class HelpSubCommand(plugin: RedeemCodeX) : JSubCommand {
    lateinit var placeHolder: CodePlaceHolder
    override var jList: List<String> = emptyList()
    override val permission: String = ""
    val config = ConfigRepository(plugin)
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)) return !sendMessage(JMessage.Command.NO_PERMISSION)


        if (args.size < 2 || args.getOrNull(1).isNullOrBlank()) return sendMessage(JMessage.Command.Help.GENERAL)

        val command = args[1].lowercase()
        when (command) {
            JTab.GeneralActions.Gen.value -> sendMessage(JMessage.Command.Help.GENERATION)
            JTab.GeneralActions.Modify.value -> sendMessage(JMessage.Command.Help.MODIFICATION)
            JTab.GeneralActions.Delete.value -> sendMessage(JMessage.Command.Help.DELETION)
            JTab.GeneralActions.Renew.value -> sendMessage(JMessage.Command.Help.RENEWAL)
            JTab.GeneralActions.Help.value -> sendMessage(JMessage.Command.Help.PREVIEW)
            JTab.GeneralActions.Info.value -> sendMessage(JMessage.Command.Help.USAGE)
            "permissions" -> sendMessage(JMessage.Command.Help.PERMISSIONS)
            JTab.GeneralActions.Reload.value -> sendMessage(JMessage.Command.Help.RELOAD)
            JTab.GeneralActions.Info.value -> sendMessage(JMessage.Command.INFO)
            else -> sendMessage(JMessage.Command.UNKNOWN_COMMAND)
        }
        return true
    }

    override fun sendMessage(key: String): Boolean {
        placeHolder.sentMessage = config.getMessage(key, placeHolder)
        config.sendMsg(key,placeHolder)
        return true
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return mutableListOf(
            JTab.GeneralActions.Gen.value,
            JTab.GeneralActions.Modify.value,
            JTab.GeneralActions.Delete.value,
            JTab.GeneralActions.Renew.value,
            JTab.GeneralActions.Help.value,
            JTab.GeneralActions.Info.value,
            JTab.GeneralActions.Reload.value,
            "permissions"
        )
    }
}