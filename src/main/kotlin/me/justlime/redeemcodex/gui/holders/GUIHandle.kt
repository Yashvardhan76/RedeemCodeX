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

package me.justlime.redeemcodex.gui.holders

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

interface GUIHandle {
    fun loadContent()
    fun onClick(event: InventoryClickEvent, clickedInventory: Inventory, player: Player)
    fun onOpen(event: InventoryOpenEvent, player: Player)
    fun onClose(event: InventoryCloseEvent, player: Player)
}