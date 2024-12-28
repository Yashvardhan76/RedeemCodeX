package me.justlime.redeemX.listener

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.gui.InventoryManager
import me.justlime.redeemX.gui.RewardsGuiHolder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent

class InventoryOpenListener(val plugin: RedeemX): Listener {
    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent ){
        if(event.inventory.holder is RewardsGuiHolder){
            val holder = event.inventory.holder as RewardsGuiHolder
            val player = holder.inventory.viewers.first() as Player
            InventoryManager.loadInventory(holder.inventory)
        }
    }
}