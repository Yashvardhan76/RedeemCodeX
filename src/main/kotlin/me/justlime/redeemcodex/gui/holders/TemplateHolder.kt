/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 */

package me.justlime.redeemcodex.gui.holders

import me.justlime.redeemcodex.api.RedeemXAPI
import me.justlime.redeemcodex.enums.JFiles
import me.justlime.redeemcodex.enums.JProperty
import me.justlime.redeemcodex.gui.InventoryManager
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.models.RedeemTemplate
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class TemplateHolder(val player: Player, row: Int, title: String, val redeemTemplate: RedeemTemplate) : InventoryHolder, GUIHandle {
    private val inventory = Bukkit.createInventory(this, row * 9, title)
    private val templateSection = getTemplateSection()
    private val itemContainer = mutableListOf<String>(
        "limit", "redemption", "commands", "rewards", "permission", "duration","cooldown" ,"pin", "messages", "sounds","condition"
    )
    private val itemSlotMap = mutableMapOf<String, List<Int>>()

    override fun getInventory(): Inventory {
        return inventory
    }

    override fun loadContent() {
        return
    }

    override fun onClick(event: InventoryClickEvent, clickedInventory: Inventory, player: Player) {
        if (clickedInventory != inventory) return
        event.isCancelled = true
        val clickedSlot = event.rawSlot
        val itemKey = itemSlotMap.entries.firstOrNull { it.value.contains(clickedSlot) }?.key ?: return
        when (itemKey) {
            "limit" -> {
                InventoryManager.openValueHolder(player,redeemTemplate,JProperty.PLAYER_LIMIT)
            }

            "redemption" -> {
                InventoryManager.openValueHolder(player,redeemTemplate,JProperty.REDEMPTION)
            }

            "commands" -> {
                player.performCommand("rcx modify template ${redeemTemplate.name} list_command")
            }

            "rewards" -> {
                player.performCommand("rcx modify template ${redeemTemplate.name} reward")
            }

            "permission" -> {
                player.performCommand("rcx modify template ${redeemTemplate.name} set_permission")
            }

            "duration" -> {
                player.performCommand("rcx modify template ${redeemTemplate.name} set_duration 1d")
            }

            "cooldown" -> {
                player.performCommand("rcx modify template ${redeemTemplate.name} set_cooldown 1d")
            }

            "pin" -> {
                player.performCommand("rcx modify template ${redeemTemplate.name} set_pin 1")
            }

            "messages" -> {
                player.performCommand("rcx modify template ${redeemTemplate.name} message")
            }

            "sounds" -> {
                player.performCommand("rcx modify template ${redeemTemplate.name} sound")
            }
            "condition" -> {
                player.performCommand("rcx modify template ${redeemTemplate.name} set_permission")
            }
        }
    }

    override fun onOpen(event: InventoryOpenEvent, player: Player) {
        InventoryManager.outlineInventory(inventory)
        itemContainer.forEach {
            val section = templateSection.getConfigurationSection(it) ?: templateSection.createSection(it)
            itemSlotMap[it] = InventoryManager.loadItem(section, inventory, CodePlaceHolder.applyByTemplate(redeemTemplate, player))
        }
    }

    override fun onClose(event: InventoryCloseEvent, player: Player) {
        return
    }

    private fun getTemplateSection() = RedeemXAPI.getPlugin().configManager.getConfig(JFiles.GUI).getConfigurationSection("template")
        ?: RedeemXAPI.getPlugin().configManager.getConfig(JFiles.GUI).createSection("template")

}