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

package me.justlime.redeemcodex.gui.holders

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.api.RedeemXAPI
import me.justlime.redeemcodex.enums.JFiles
import me.justlime.redeemcodex.enums.JProperty
import me.justlime.redeemcodex.enums.RedeemType
import me.justlime.redeemcodex.gui.InventoryManager
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.models.MessageState
import me.justlime.redeemcodex.models.Title
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class MessageHolder(
    private val plugin: RedeemCodeX, val player: Player, private val redeemData: RedeemType, row: Int, title: String
) : InventoryHolder, GUIHandle {

    private val inventory = Bukkit.createInventory(this, row * 9, title)
    private var messageState = MessageState(mutableListOf(), "", Title())
    private val guiConfig = plugin.configManager.getConfig(JFiles.GUI)

    private val placeholder = when (redeemData) {
        is RedeemType.Code -> {
            messageState = redeemData.redeemCode.messages
            CodePlaceHolder.applyByRedeemCode(redeemData.redeemCode, player)
        }

        is RedeemType.Template -> {
            messageState = redeemData.redeemTemplate.messages
            CodePlaceHolder.applyByTemplate(redeemData.redeemTemplate, player)
        }
    }
    private var inputReceived = false
    private val listener = plugin.listenerManager.asyncPlayerChatListener

    override fun getInventory(): Inventory = inventory

    override fun loadContent() {
        inventory.clear()
        InventoryManager.outlineInventory(inventory, 3)
        loadMainMenu()
    }

    private fun getRedeemName(): String = when (redeemData) {
        is RedeemType.Code -> redeemData.redeemCode.code
        is RedeemType.Template -> redeemData.redeemTemplate.name
    }

    override fun onClick(event: InventoryClickEvent, clickedInventory: Inventory, player: Player) {
        event.isCancelled = true
        val guiConfig = plugin.configManager.getConfig(JFiles.GUI)
        val messageItemSlot =
            (guiConfig.getConfigurationSection("messages.message.item") ?: guiConfig.createSection("messages.message.item")).getInt("slot", 10)
        val actionBarItemSlot =
            (guiConfig.getConfigurationSection("messages.actionbar.item") ?: guiConfig.createSection("messages.actionbar.item").getInt("slot", 13))
        val titleItemSlot =
            (guiConfig.getConfigurationSection("messages.title.item") ?: guiConfig.createSection("messages.title.item")).getInt("slot", 16)
        val saveItemSlot =
            (guiConfig.getConfigurationSection("messages.save.item") ?: guiConfig.createSection("messages.save.item")).getInt("slot", 22)


        if (event.click == ClickType.LEFT) {
            when (event.slot) {
                messageItemSlot -> openInputText()
                actionBarItemSlot -> openInputActionBar()
                titleItemSlot -> openInputTitle()
                saveItemSlot -> {
                    upsertMessage()
                    player.sendMessage("§aMessages saved!")
                }
            }
        }
    }

    override fun onOpen(event: InventoryOpenEvent, player: Player) {
        loadContent()
    }

    override fun onClose(event: InventoryCloseEvent, player: Player) {}

    private fun loadMainMenu() {

        val messageItem = guiConfig.getConfigurationSection("messages.message.item") ?: guiConfig.createSection("messages.message.item")
        val actionBarItem = guiConfig.getConfigurationSection("messages.actionbar.item") ?: guiConfig.createSection("messages.actionbar.item")
        val titleItem = guiConfig.getConfigurationSection("messages.title.item") ?: guiConfig.createSection("messages.title.item")
        val saveItem = guiConfig.getConfigurationSection("messages.save.item") ?: guiConfig.createSection("messages.save.item")

        listOf(messageItem, actionBarItem, titleItem, saveItem).forEachIndexed { index, item ->
            val material = item.getString("material")?.let { Material.valueOf(it) } ?: Material.PAPER
            val name = JService.applyColors(item.getString("name") ?: "").replace("{code}", getRedeemName())
            val lore = item.getStringList("lore").map {
                JService.applyColors(it).replace("{code}", getRedeemName())
            }
            val slot = item.getInt("slot", 10 + index)
            val glint = item.getBoolean("glint", false)
            inventory.setItem(slot, InventoryManager.createItem(material, name, lore, glint))
        }
    }

    private fun openInputText() {

        val timeout = guiConfig.getConfigurationSection("messages.message")?.getLong("timeout") ?: 240

        inputReceived = false
        closeInventoryWithMessage(player, "§eEnter your messages in chat. Type 'done' when you're finished:")
        listener.registerCallback(player, timeout * 20, onTimeout = {
            handleInputTimeout("§cInput timed out. Returning to menu.")
        }, callback = { message ->
            if (message.equals("done", ignoreCase = true)) {
                finalizeInput(player, "§aChat messages updated.")
                listener.unregisterCallback(player)
            } else {
                val formattedMessage = JService.applyColors(message)
                messageState.text.add(formattedMessage)
                player.sendMessage("§aAdded: $formattedMessage")
            }
        })
    }

    private fun openInputActionBar() {
        inputReceived = false
        closeInventoryWithMessage(player, "§eEnter your action bar message:")
        val timeout = guiConfig.getConfigurationSection("messages.actionbar")?.getLong("timeout") ?: 60

        listener.registerCallback(player, timeout * 20, {
            handleInputTimeout("§cAction bar message input timed out.")
        }, callback = { message ->
            messageState.actionbar = JService.applyColors(message)
            finalizeInput(player, "§aActionbar message set to: ${messageState.actionbar}")
        })
    }

    private fun openInputTitle() {
        inputReceived = false
        closeInventoryWithMessage(player, "§eEnter your title message (format: title;subtitle;fadeIn;stay;fadeOut):")
        val timeout = guiConfig.getConfigurationSection("messages.title")?.getLong("timeout") ?: 60

        listener.registerCallback(player, timeout * 20, {
            handleInputTimeout("§cTitle message input timed out.")
        }, callback = { message ->
            val parts = message.split(";")
            if (parts.size == 5) {
                val title = parseTitleInput(parts)
                if (title != null) {
                    messageState.title = title
                    finalizeInput(player, "§aTitle message updated.")
                } else {
                    player.sendMessage("§cInvalid number format. Please try again.")
                }
            } else {
                player.sendMessage("§cInvalid format. Use: title;subtitle;fadeIn;stay;fadeOut")
            }
        })
    }

    private fun parseTitleInput(parts: List<String>): Title? {
        return try {
            Title(
                title = JService.applyColors(parts[0]),
                subTitle = JService.applyColors(parts[1]),
                fadeIn = parts[2].toIntOrNull() ?: 1,
                stay = parts[3].toIntOrNull() ?: 2,
                fadeOut = parts[4].toIntOrNull() ?: 1
            )
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun handleInputTimeout(message: String) {
        plugin.listenerManager.asyncPlayerChatListener.unregisterCallback(player)
        player.sendMessage(message)
        reopenInventory(player)
    }

    private fun finalizeInput(player: Player, message: String) {
        inputReceived = true
        player.sendMessage(message)
        reopenInventory(player)
    }

    private fun reopenInventory(player: Player) {
        plugin.server.scheduler.runTask(plugin, Runnable {
            player.openInventory(inventory)
            loadContent()
        })
    }

    private fun closeInventoryWithMessage(player: Player, message: String) {
        player.closeInventory()
        player.sendMessage(message)
    }

    private fun upsertMessage() {
        when (redeemData) {
            is RedeemType.Code -> {
                redeemData.redeemCode.messages = messageState
                plugin.redeemCodeDB.upsertCode(redeemData.redeemCode)
            }

            is RedeemType.Template -> {
                redeemData.redeemTemplate.messages = messageState
                plugin.configRepo.upsertTemplate(redeemData.redeemTemplate)
                RedeemXAPI.modifyTemplate(redeemData.redeemTemplate.name, JProperty.SYNC.property, sender = player)
            }
        }
    }

}
