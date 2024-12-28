package me.justlime.redeemX.gui

import me.justlime.redeemX.enums.JTemplate
import me.justlime.redeemX.models.RedeemCode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin

object EditRewards{
    private const val REDEEM_CODE_SLOT = 49
    private val REDEEM_CODE_ITEM = Material.NETHER_STAR

    fun template(plugin: JavaPlugin, template: JTemplate, player: Player){

    }

    fun code(plugin: JavaPlugin, redeemCode: RedeemCode, player: Player){

        val rewardsInventory = RewardsGuiHolder(redeemCode,54, "Rewards GUI").getInventory()

        // Set the Redeem Code item in the designated slot
        val redeemCodeItem = ItemStack(REDEEM_CODE_ITEM, 1)
        val itemMeta: ItemMeta? = redeemCodeItem.itemMeta
        itemMeta?.apply {
            setDisplayName("Code: ${redeemCode.code}")
            lore = listOf("Redeem code associated with this GUI")
        }
        redeemCodeItem.itemMeta = itemMeta
        rewardsInventory.setItem(REDEEM_CODE_SLOT, redeemCodeItem)
        player.openInventory(rewardsInventory)
        return
    }
}