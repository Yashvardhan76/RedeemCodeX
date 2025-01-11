/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *  This file handles the core logic for redeeming codes and managing associated data.
 *
 */

package me.justlime.redeemcodex.gui.holders

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.api.RedeemXAPI
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.data.repository.RedeemCodeRepository
import me.justlime.redeemcodex.enums.JMessage
import me.justlime.redeemcodex.enums.RedeemType
import me.justlime.redeemcodex.gui.InventoryManager
import me.justlime.redeemcodex.gui.InventoryManager.selectedSlots
import me.justlime.redeemcodex.models.CodePlaceHolder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class RewardsHolder(val sender: Player, private val redeemData: RedeemType, row: Int, title: String) : InventoryHolder, GUIHandle {
    private val inventory = Bukkit.createInventory(this, row * 9, title)
    override fun getInventory(): Inventory = inventory
    override fun loadContent() {
        InventoryManager.outlineInventory(inventory)
        val redeemCodeSlot = 49
        val redeemCodeItem = ItemStack(Material.NETHER_STAR, 1)
        val itemMeta: ItemMeta? = redeemCodeItem.itemMeta
        when (redeemData) {
            is RedeemType.Code -> {
                // Set the Redeem Code item in the designated slot
                itemMeta?.apply { setDisplayName("Code: ${redeemData.redeemCode.code}") }
                redeemCodeItem.itemMeta = itemMeta
            }

            is RedeemType.Template -> {
                itemMeta?.apply { setDisplayName("Template: ${redeemData.redeemTemplate.name}") }
                redeemCodeItem.itemMeta = itemMeta
            }
        }
        inventory.setItem(redeemCodeSlot, redeemCodeItem)
    }

    override fun onClick(event: InventoryClickEvent, clickedInventory: Inventory, player: Player) {
        val playerInventory = player.inventory
        val upperInventory = event.inventory
        event.isCancelled = true
        if (event.clickedInventory == upperInventory && event.slot in InventoryManager.outlinedSlotsFull) return
        if (clickedInventory === playerInventory) handlePlayerInventoryClick(event, player, upperInventory)
        else if (clickedInventory === upperInventory) handleGuiClick(event, player, upperInventory)
        else return
    }

    override fun onOpen(event: InventoryOpenEvent, player: Player) {
        loadContent()
        val holder = inventory.holder as RewardsHolder
        player.sendMessage("Loading rewards...")

        val savedRewards = when (holder.redeemData) {
            is RedeemType.Code -> {
                holder.redeemData.redeemCode.rewards
            }

            is RedeemType.Template -> {
                holder.redeemData.redeemTemplate.rewards
            }
        }

        // Loop over the selectedSlots and assign items to each slot
        var index = 0
        repeat(savedRewards.size) {
            // Check if there are still rewards to assign
            inventory.setItem(selectedSlots[index], savedRewards[it])
            index++
            if (index >= selectedSlots.size) return@repeat
        }
    }

    override fun onClose(event: InventoryCloseEvent, player: Player) {
        saveInventoryItems(RedeemXAPI.getPlugin(), inventory)
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
        resortInventory(rewardsInventory) // Resort rewards GUI
    }

    private fun saveInventoryItems(plugin: RedeemCodeX, inventory: Inventory): Boolean {
        val holder = inventory.holder as RewardsHolder

        val player = if (inventory.viewers.first() is Player) holder.inventory.viewers.first() as Player else return false
        player.sendMessage("Saving rewards...")

        val codeRepo = RedeemCodeRepository(plugin) //Used to update the database
        val configRepo = ConfigRepository(plugin) //Used to send the msg

        val items: MutableList<ItemStack?> = holder.inventory.contents.filterIndexed { index, _ -> index in selectedSlots }.toMutableList()
        items.removeIf { it == null || it.type == Material.AIR }
        when (redeemData) {
            is RedeemType.Code -> {
                val redeemCode = (holder.redeemData as RedeemType.Code).redeemCode
                val placeHolder = CodePlaceHolder(player, code = redeemCode.code)
                try {
                    redeemCode.rewards = items.filterNotNull().toMutableList()
                    val success = codeRepo.upsertCode(redeemCode)
                    if (!success) {
                        configRepo.sendMsg(JMessage.Code.Modify.FAILED, placeHolder)
                        return false
                    }
                    configRepo.sendMsg(JMessage.Code.Gui.Save.REWARDS, placeHolder)
                    return true
                } catch (e: Exception) {
                    configRepo.sendMsg(JMessage.Code.Modify.FAILED, placeHolder)
                    return false
                }
            }

            is RedeemType.Template -> {
                val redeemTemplate = (holder.redeemData as RedeemType.Template).redeemTemplate
                val placeHolder = CodePlaceHolder(player, template = redeemTemplate.name)
                try {
                    redeemTemplate.rewards = items.filterNotNull().toMutableList()
                    val success = configRepo.upsertTemplate(redeemTemplate)
                    if (!success) {
                        configRepo.sendMsg(JMessage.Template.Modify.FAILED, placeHolder)
                        return false
                    }
                    configRepo.sendMsg(JMessage.Template.Gui.Save.REWARDS, placeHolder)
                    return true
                } catch (e: Exception) {
                    configRepo.sendMsg(JMessage.Template.Modify.FAILED, placeHolder)
                    return false
                }

            }
        }

    }

    private fun resortInventory(inventory: Inventory) {
        val rewards = mutableListOf<ItemStack?>()

        // Collect items from the rows
        for (index in selectedSlots) {
            val item = inventory.getItem(index)
            if (item != null) {
                rewards.add(item)
            }
            inventory.setItem(index, null)
        }

        // Add items back to the inventory, starting from the first slot
        var index = 0
        for (item in rewards) {
            if (item != null) {
                inventory.setItem(selectedSlots[index], item)
                index++
            }
        }
    }
}
// hello yash, i am here; what u doing ? plz
