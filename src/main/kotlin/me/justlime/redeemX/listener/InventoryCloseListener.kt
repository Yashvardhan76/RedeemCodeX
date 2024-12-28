package me.justlime.redeemX.listener

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.gui.InventoryManager
import me.justlime.redeemX.gui.RewardsGuiHolder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryCloseListener(val plugin: RedeemX) : Listener {

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val inventory = event.inventory
        val player = event.player as? Player ?: return

        // Check if it's the GUI you want to track
        if (inventory.holder is RewardsGuiHolder) {

            // Trigger your function
            InventoryManager.saveInventoryItems(plugin,inventory)
        }
    }

}