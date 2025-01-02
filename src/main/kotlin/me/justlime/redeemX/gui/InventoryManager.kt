package me.justlime.redeemX.gui

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object InventoryManager {
    val selectedSlots = listOf(10..16, 19..25, 28..34, 37..43).flatMap { it.step(1).toList() }
    val outlinedSlots = listOf(0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53)

    fun outlineInventory(inventory: Inventory) {
        val item = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        outlinedSlots.filter{it!=49}.forEach { slot ->
            inventory.setItem(slot, item)
        }
    }

    fun saveInventoryItems(plugin: RedeemX, inventory: Inventory): Boolean {

        if (inventory.holder is RewardsGuiHolder) {
            val holder = inventory.holder as RewardsGuiHolder
            val redeemCode = holder.redeemCode

            val player = if (inventory.viewers.first() is Player) holder.inventory.viewers.first() as Player else return false
            player.sendMessage("Saving rewards...")

            val placeHolder = CodePlaceHolder(player, code = redeemCode.code)

            val codeRepo = RedeemCodeRepository(plugin) //Used to update the database
            val configRepo = ConfigRepository(plugin) //Used to send the msg

            val items: MutableList<ItemStack?> = holder.inventory.contents.filterIndexed { index, _ -> index in selectedSlots }.toMutableList()
            items.removeIf { it == null || it.type == Material.AIR }

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
        return false
    }

    fun loadInventory(inventory: Inventory) {
        when (inventory.holder) {
            is RewardsGuiHolder -> {
                outlineInventory(inventory)
                val holder = inventory.holder as RewardsGuiHolder
                val player = if (inventory.viewers.first() is Player) holder.inventory.viewers.first() as Player else return
                player.sendMessage("Loading rewards...")

                val redeemCode = holder.redeemCode

                // Define the slots in ranges
                val savedRewards = redeemCode.rewards

                // Loop over the selectedSlots and assign items to each slot
                var index = 0
                repeat(savedRewards.size) {
                    // Check if there are still rewards to assign
                    inventory.setItem(selectedSlots[index], savedRewards[it])
                    index++
                    if (index >= selectedSlots.size) return@repeat
                }
            }
        }
    }

    fun resortInventory(inventory: Inventory) {
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