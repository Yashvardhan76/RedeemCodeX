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

package me.justlime.redeemcodex.gui

import me.justlime.redeemcodex.api.RedeemXAPI
import me.justlime.redeemcodex.enums.JFiles
import me.justlime.redeemcodex.enums.JProperty
import me.justlime.redeemcodex.enums.RedeemType
import me.justlime.redeemcodex.gui.holders.TemplateHolder
import me.justlime.redeemcodex.gui.holders.ValueHolder
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.models.RedeemTemplate
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object InventoryManager {
    val pluginInstance = RedeemXAPI.getPlugin()
    val isPlaceHolderHooked = { RedeemXAPI.getPlugin().server.pluginManager.isPluginEnabled("PlaceholderAPI") }
    val selectedSlots = listOf(10..16, 19..25, 28..34, 37..43).flatMap { it.step(1).toList() }
    val outlinedSlotsFull = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
    val outlinedSlotsHalf = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
    var timeOut: Long = 600L //In seconds

    fun outlineInventory(inventory: Inventory): List<Int> {
        val item = Material.valueOf(pluginInstance.configManager.getConfig(JFiles.GUI).getString("outline.material") ?: "GRAY_STAINED_GLASS_PANE")
        val itemStack = ItemStack(item)
        val itemMeta = itemStack.itemMeta.apply {
            this?.itemFlags?.clear()
        }
        itemStack.itemMeta = itemMeta
        val slots = mutableListOf<Int>()
        for (i in 0 until inventory.size) {
            if (i in 0..8 || i >= inventory.size - 9 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inventory.setItem(i, itemStack)
                slots.add(i)
            }
        }
        return slots
    }

    fun createItem(material: Material, name: String, lore: List<String>, glint: Boolean, flags: MutableList<String> = mutableListOf(),qty: Int = 0):
            ItemStack {
        return ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName(name)
                setLore(lore) // Use setLore() for better compatibility

                // Apply enchantment for glint effect
                if (glint) {
                    addEnchant(Enchantment.UNBREAKING, 1, true)
                }
                if (flags.isNotEmpty()) {
                    flags.forEach {
                        try {
                            addItemFlags(ItemFlag.valueOf(it))
                        } catch (e: IllegalArgumentException) {
                            RedeemXAPI.getPlugin().logger.warning("Unknown flag: $it at item: $name")
                        }
                    }
                }

            }
            amount = qty
        }
    }

    fun loadItem(
        section: ConfigurationSection,
        inventory: Inventory,
        placeHolder: CodePlaceHolder,
        slots: List<Int> = listOf(),
        lore: MutableList<String> = mutableListOf()
    ): List<Int> {
        val flags = section.getStringList("flags")
        val material = try {
            Material.valueOf(section.getString("item") ?: "PAPER")
        } catch (e: Exception) {
            pluginInstance.logger.warning("Invalid material: ${section.getString("item")} at item: ${section.getString("name")}")
            Material.PAPER
        }
        val nameWithPH = JService.applyPlaceholders(section.getString("name") ?: "", placeHolder, isPlaceHolderHooked)
        val name = JService.applyHexColors(nameWithPH)
        val newLoreWithPH = if (lore.isEmpty()) section.getStringList("lore")
            .map { JService.applyPlaceholders(it ?: "", placeHolder, isPlaceHolderHooked) } else lore
        val newLore = newLoreWithPH.map { JService.applyHexColors(it) }
        val glow = section.getBoolean("glow")
        val slotList = section.getIntegerList("slot")
        val item = createItem(material, name, newLore, glow, flags)
        if (slots.isNotEmpty()) {
            slots.forEach { inventory.setItem(it, item) }
            return slots
        }
        if (slotList.isNotEmpty()) {
            try {
                slotList.forEach {
                    inventory.setItem(it, item)
                }
                return slotList
            } catch (e: Exception) {
                pluginInstance.logger.warning("Invalid slot: $slotList at item: $nameWithPH")
            }
        }
        try {
            val slot = section.getString("slot", " ")?.toIntOrNull()
            if (slot == null) return listOf()
            inventory.setItem(slot, item)
            return listOf(slot)
        } catch (e: Exception) {
            pluginInstance.logger.warning("Invalid slot: ${section.getString("slot")} at item: ${section.getString("name")}")
            return listOf()
        }
    }

    fun createCertainItem(section: ConfigurationSection, itemSlot: Int, itemSlots: List<Int>, inventory: Inventory) {
        val backMaterial = Material.valueOf(section.getString("item") ?: "PAPER")
        val backName = JService.applyHexColors(section.getString("name") ?: " ")
        val backLore = section.getStringList("lore").map { JService.applyHexColors(it) }
        val backGlow = section.getBoolean("glow")
        val flags = section.getStringList("flags")
        if (itemSlots.isNotEmpty()) {
            itemSlots.forEach { inventory.setItem(it, createItem(backMaterial, backName, backLore, backGlow, flags)) }
        }
        inventory.setItem(itemSlot, createItem(backMaterial, backName, backLore, backGlow, flags))
    }

    fun openTemplateSetting(player: Player, redeemTemplate: RedeemTemplate): Boolean {
        val row = 6
        val title = "Editing " + redeemTemplate.name
        val inventory = TemplateHolder(player,row,title,redeemTemplate).inventory
        player.openInventory(inventory)
        return true
    }

    fun openValueHolder(player: Player,redeemTemplate: RedeemTemplate,property: JProperty): Boolean {
        val row = 6
        val title = "Editing " + redeemTemplate.name
        val inventory = ValueHolder(row,title,RedeemType.Template(redeemTemplate),property).inventory
        player.openInventory(inventory)
        return true
    }

}