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
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object InventoryManager {
    val selectedSlots = listOf(10..16, 19..25, 28..34, 37..43).flatMap { it.step(1).toList() }
    val outlinedSlotsFull = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
    val outlinedSlotsHalf = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
    var timeOut: Long = 600L //In seconds

    fun outlineInventory(inventory: Inventory, size: Int = 6) {
        val guiConfig = RedeemXAPI.getPlugin().configManager.getConfig(JFiles.GUI)
        val borderItem = guiConfig.getConfigurationSection("border.item") ?: return
        val material = borderItem.getString("material")
            ?.let { if (Material.valueOf(it) == Material.AIR) Material.RED_STAINED_GLASS_PANE else Material.valueOf(it) }
            ?: Material.RED_STAINED_GLASS_PANE
        val name = JService.applyColors(borderItem.getString("name") ?: "")
        val lore = borderItem.getStringList("lore").map { JService.applyColors(it) }
        val glint = borderItem.getBoolean("glint", false)
        val item = createItem(material, name, lore, glint)

        if (size == 6) outlinedSlotsFull.forEach { inventory.setItem(it, item) }
        if (size == 3) outlinedSlotsHalf.forEach { inventory.setItem(it, item) }
    }

    fun createItem(material: Material, name: String, lore: List<String>, glint: Boolean): ItemStack {
        return ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName(name)
                if (glint) {
                    this.addEnchant(Enchantment.UNBREAKING, 1, true)
                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
                this.lore = lore
            }
        }
    }

}