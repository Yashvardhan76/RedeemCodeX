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
import me.justlime.redeemcodex.gui.holders.RewardsHolder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent

class InventoryOpenListener(val plugin: RedeemCodeX) : Listener {
    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val upperInventory = event.inventory
        val player = event.player as? Player ?: return
        val holder = upperInventory.holder
        if (holder is GUIHandle) holder.onOpen(event, player)

    }
}