package me.justlime.redeemX.listener

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.gui.InventoryManager
import me.justlime.redeemX.gui.RewardsGuiHolder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

class InventoryClickListener(plugin: RedeemX) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val clickedInventory = event.clickedInventory ?: return
        val player = event.whoClicked as? Player ?: return
        val upperInventory = event.inventory
        val playerInventory = player.inventory

        // Check if the inventory is the Rewards GUI
        when(upperInventory.holder){
            is RewardsGuiHolder ->{
                event.isCancelled = true
                if (event.clickedInventory == upperInventory && event.slot in InventoryManager.outlinedSlots) return

                if (clickedInventory === playerInventory) handlePlayerInventoryClick(event, player, upperInventory)
                else if (clickedInventory === upperInventory) handleGuiClick(event, player, upperInventory)
                else return

            }
        }
    }

    private fun handlePlayerInventoryClick(event: InventoryClickEvent, player: Player, rewardsInventory: Inventory) {
        val clickedItem = event.currentItem ?: return

        val firstEmptySlot = rewardsInventory.firstEmpty()
        if (firstEmptySlot == -1) {
            player.sendMessage("The rewards inventory is full!")
            return
        }

        rewardsInventory.setItem(firstEmptySlot, clickedItem) // Add item to GUI
        player.inventory.clear(event.slot) // Remove from player inventory
    }

    private fun handleGuiClick(event: InventoryClickEvent, player: Player, rewardsInventory: Inventory) {
        val clickedItem = event.currentItem ?: return
        if (clickedItem.type == Material.AIR) return
        if (event.slot >= 45) return

        if (player.inventory.firstEmpty() == -1) {
            player.sendMessage("Your inventory is full!")
            return
        }

        player.inventory.addItem(clickedItem) // Add to player inventory
        rewardsInventory.setItem(event.slot, null) // Remove from GUI
        InventoryManager.resortInventory(rewardsInventory) // Resort rewards GUI
    }


}
