package me.justlime.redeemcodex.gui

import me.justlime.redeemcodex.gui.holders.GUIHandle
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

object InventoryManager {
    val selectedSlots = listOf(10..16, 19..25, 28..34, 37..43).flatMap { it.step(1).toList() }
    val outlinedSlots = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)

    fun outlineInventory(inventory: Inventory) {
        val item = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        outlinedSlots.filter { it != 49 }.forEach { slot ->
            inventory.setItem(slot, item)
        }
    }
}