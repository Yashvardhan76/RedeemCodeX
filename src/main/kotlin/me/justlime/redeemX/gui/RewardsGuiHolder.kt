package me.justlime.redeemX.gui

import me.justlime.redeemX.models.RedeemCode
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class RewardsGuiHolder(val redeemCode: RedeemCode, size: Int, title: String): InventoryHolder {
    private val inventory = Bukkit.createInventory(this, size, title)
    override fun getInventory(): Inventory = inventory
}