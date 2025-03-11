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

import me.justlime.redeemcodex.enums.JFiles
import me.justlime.redeemcodex.enums.JProperty
import me.justlime.redeemcodex.enums.RedeemType
import me.justlime.redeemcodex.gui.InventoryManager
import me.justlime.redeemcodex.gui.InventoryManager.pluginInstance
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class ValueHolder(val row: Int, val title: String, val type: RedeemType, property: JProperty) : InventoryHolder, GUIHandle {
    private val inventory = Bukkit.createInventory(this, row * 9, title)
    private val currentValue = when (type) {
        is RedeemType.Template -> {
            when (property) {
                JProperty.PLAYER_LIMIT -> type.redeemTemplate.playerLimit
                JProperty.REDEMPTION -> type.redeemTemplate.redemption
                else -> 0
            }
        }

        is RedeemType.Code -> {
            when (property) {
                JProperty.PLAYER_LIMIT -> type.redeemCode.playerLimit
                JProperty.REDEMPTION -> type.redeemCode.redemption
                else -> 0
            }
        }
    }

    override fun getInventory() = inventory

    override fun loadContent() {
        return
    }

    override fun onClick(event: InventoryClickEvent, clickedInventory: Inventory, player: Player) {
        event.isCancelled = true
        when (type) {
            is RedeemType.Template -> {

            }

            is RedeemType.Code -> {

            }
        }
    }

    override fun onOpen(event: InventoryOpenEvent, player: Player) {
        InventoryManager.outlineInventory(inventory)
        val incrementMaterial = pluginInstance.configManager.getConfig(JFiles.GUI).getString("increment.material") ?: "PAPER"
        val decrementMaterial = pluginInstance.configManager.getConfig(JFiles.GUI).getString("decrement.material") ?: "PAPER"
        val incrementName = pluginInstance.configManager.getConfig(JFiles.GUI).getString("increment.name") ?: " "
        val decrementName = pluginInstance.configManager.getConfig(JFiles.GUI).getString("decrement.name") ?: " "
        val incrementLore = pluginInstance.configManager.getConfig(JFiles.GUI).getStringList("increment.lore")
        val decrementLore = pluginInstance.configManager.getConfig(JFiles.GUI).getStringList("decrement.lore")
        val incrementGlow = pluginInstance.configManager.getConfig(JFiles.GUI).getBoolean("increment.glow")
        val decrementGlow = pluginInstance.configManager.getConfig(JFiles.GUI).getBoolean("decrement.glow")
        val incrementFlags = pluginInstance.configManager.getConfig(JFiles.GUI).getStringList("increment.flags")
        val decrementFlags = pluginInstance.configManager.getConfig(JFiles.GUI).getStringList("decrement.flags")
        val incrementItem1 = InventoryManager.createItem(

            Material.valueOf(incrementMaterial),
            incrementName.replace("{value}", "1"),
            incrementLore,
            incrementGlow,
            incrementFlags.toMutableList(),
            1
        )
        val incrementItem2 = InventoryManager.createItem(
            Material.valueOf(incrementMaterial),
            incrementName.replace("{value}", "10"),
            incrementLore,
            incrementGlow,
            incrementFlags.toMutableList(),
            10
        )
        val incrementItem3 = InventoryManager.createItem(
            Material.valueOf(incrementMaterial),
            incrementName.replace("{value}", "100"),
            incrementLore,
            incrementGlow,
            incrementFlags.toMutableList(),
            64
        )
        val decrementItem1 = InventoryManager.createItem(
            Material.valueOf(decrementMaterial),
            decrementName.replace("{value}", "1"),
            decrementLore,
            decrementGlow,
            decrementFlags.toMutableList(),
            1
        )
        val decrementItem2 = InventoryManager.createItem(
            Material.valueOf(decrementMaterial),
            decrementName.replace("{value}", "10"),
            decrementLore,
            decrementGlow,
            decrementFlags.toMutableList(),
            10
        )
        val decrementItem3 = InventoryManager.createItem(
            Material.valueOf(decrementMaterial),
            decrementName.replace("{value}", "100"),
            decrementLore,
            decrementGlow,
            decrementFlags.toMutableList(),
            64
        )

        inventory.setItem(10, incrementItem1)
        inventory.setItem(11, incrementItem2)
        inventory.setItem(12, incrementItem3)
        inventory.setItem(14, decrementItem1)
        inventory.setItem(15, decrementItem2)
        inventory.setItem(16, decrementItem3)

        return
    }

    override fun onClose(event: InventoryCloseEvent, player: Player) {
        when (type) {
            is RedeemType.Template -> {
//                InventoryManager.openTemplateSetting(player, type.redeemTemplate)
            }

            is RedeemType.Code -> {

            }
        }
    }
}