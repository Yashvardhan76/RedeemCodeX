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

package me.justlime.redeemcodex.models

import me.justlime.redeemcodex.utilities.JService
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player

data class MessageState(
    var text: MutableList<String>, var actionbar: String, var title: Title
) {
    fun sendMessage(player: Player, placeHolder: CodePlaceHolder, isPlaceholderHooked: () -> Boolean = { false }) {
        actionbar.let {
            val newBar = JService.applyHexColors(JService.applyPlaceholders(it, placeHolder, isPlaceholderHooked))
            if (it.isNotEmpty()) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(newBar.removePrefix(" ")))
        }
        title.let {
            val newTitle = JService.applyHexColors(JService.applyPlaceholders(it.title, placeHolder, isPlaceholderHooked))
            val newSubTitle = JService.applyHexColors(JService.applyPlaceholders(it.subTitle, placeHolder, isPlaceholderHooked))
            if (it.title.isNotEmpty()) player.sendTitle(newTitle.removePrefix(" "), newSubTitle, it.fadeIn, it.stay, it.fadeOut)
        }
        text.let {
            if (it.isNotEmpty()) it.forEach { msg ->
                val newMsg = JService.applyHexColors(JService.applyPlaceholders(msg, placeHolder, isPlaceholderHooked))
                player.sendMessage(newMsg)
            }
        }
    }
}