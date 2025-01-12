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

package me.justlime.redeemcodex.listener

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.gui.holders.GUIHandle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener(plugin: RedeemCodeX) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val clickedInventory = event.clickedInventory ?: return
        val player = event.whoClicked as? Player ?: return
        val upperInventory = event.inventory
        val holder = upperInventory.holder
        if (holder is GUIHandle) holder.onClick(event, clickedInventory, player)
    }

}
