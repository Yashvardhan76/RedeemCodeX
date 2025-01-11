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


package me.justlime.redeemcodex.commands

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.data.repository.RedeemCodeRepository
import me.justlime.redeemcodex.enums.JConfig
import me.justlime.redeemcodex.enums.JMessage
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.utilities.CodeValidation
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class RedeemCommand(
    private val plugin: RedeemCodeX,
) : CommandExecutor, TabCompleter {
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        var placeHolder = CodePlaceHolder(sender, args.toMutableList())
        if (sender !is Player) {
            config.sendMsg(JMessage.Command.RESTRICTED_TO_PLAYERS, placeHolder)
            return true
        }

        if (args.isEmpty()) {
            config.sendMsg(JMessage.Redeem.USAGE, placeHolder)
            return true
        }
        placeHolder.sender = sender
        placeHolder.code = args[0].uppercase()
        val codeValidation = CodeValidation(plugin, args[0].uppercase(), sender)
        if (!codeValidation.isCodeExist()) {
            config.sendMsg(JMessage.Redeem.INVALID_CODE, placeHolder)
            return true
        }
        placeHolder = CodePlaceHolder.applyByRedeemCode(codeValidation.redeemCode, sender)

        if (codeValidation.isReachedMaximumRedeem(sender)) {
            config.sendMsg(JMessage.Redeem.MAX_REDEMPTIONS, placeHolder)
            return true
        }

        if (codeValidation.isReachedMaximumPlayer()) {
            config.sendMsg(JMessage.Redeem.MAX_PLAYER_REDEEMED, placeHolder)
            return true
        }

        if (!codeValidation.hasPermission(sender)) {
            placeHolder.permission = codeValidation.redeemCode.permission
            config.sendMsg(JMessage.Redeem.NO_PERMISSION, placeHolder)
            return true
        }

        if (!codeValidation.isCodeEnabled()) {
            config.sendMsg(JMessage.Redeem.DISABLED, placeHolder)
            return true
        }

        if (codeValidation.isCodeExpired()) {
            config.sendMsg(JMessage.Redeem.EXPIRED_CODE, placeHolder)
            return true
        }

        // Target validation
        if (!codeValidation.isTargetValid(sender.name)) {
            config.sendMsg(JMessage.Redeem.INVALID_TARGET, placeHolder)
            return true
        }

        if (codeValidation.isPinRequired()) {
            if (args.size < 2) {
                config.sendMsg(JMessage.Redeem.MISSING_PIN, placeHolder)
                return true
            }

            val pin = args[1].toIntOrNull() ?: 0
            placeHolder.pin = pin.toString()
            if (!codeValidation.isCorrectPin(pin)) {
                config.sendMsg(JMessage.Redeem.INVALID_PIN, placeHolder)
                return true
            }
        }

        val redeemCode = codeValidation.redeemCode
        if(codeValidation.isCooldown(placeHolder)) {
            config.sendMsg(JMessage.Redeem.ON_COOLDOWN, placeHolder)
            return true
        }

        if (redeemCode.rewards.size > getEmptySlotSize(sender) && config.getConfigValue(JConfig.Rewards.DROP) == "false") {
            config.sendMsg(JMessage.Redeem.FULL_INVENTORY, placeHolder)
            return true
        }

        // MAIN STUFF

        redeemCode.usedBy[sender.name] = (redeemCode.usedBy[sender.name]?.plus(1)) ?: 1
        redeemCode.lastRedeemed[sender.name] = JService.getCurrentTime()

        val success = codeRepo.upsertCode(redeemCode)
        if (!success) {
            config.sendMsg(JMessage.Redeem.FAILED, placeHolder)
            return false
        }

        //Execute Command
        val console = plugin.server.consoleSender
        redeemCode.commands.forEach {
            val cmd = JService.applyColors(JService.applyPlaceholders(it, CodePlaceHolder.applyByRedeemCode(redeemCode, placeHolder.sender)) {
                plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")
            })
            //set using placeholder api
            plugin.server.dispatchCommand(console, cmd)
        }

        //Received Messages
        config.sendTemplateMsg(redeemCode.template, placeHolder)

        //Received Rewards
        redeemCode.rewards.forEach { item ->
            val remaining = sender.inventory.addItem(item)
            if (remaining.isEmpty()) return@forEach
            if (config.getConfigValue(JConfig.Rewards.SOUND) == "true") sender.playSound(sender.location, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)

            // If there are remaining items (inventory was full), drop them
            remaining.values.forEach { droppedItem ->
                sender.world.dropItem(sender.location, droppedItem)
            }
        }
        if(redeemCode.sound.sound != null) redeemCode.sound.playSound(sender)
        if(redeemCode.messages.text.isNotEmpty()) redeemCode.messages.sendMessage(sender,placeHolder)
        else config.sendMsg(JMessage.Redeem.SUCCESS, placeHolder)
        return true


    }

    private fun getEmptySlotSize(sender: Player): Int = sender.inventory.filter { it == null }.size

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> = emptyList()
}
