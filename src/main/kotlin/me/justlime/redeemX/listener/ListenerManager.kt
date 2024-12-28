package me.justlime.redeemX.listener

import me.justlime.redeemX.RedeemX
import org.bukkit.plugin.java.JavaPlugin

class ListenerManager(plugin: RedeemX) {
    init {
        plugin.server.pluginManager.registerEvents(InventoryClickListener(plugin), plugin)
        plugin.server.pluginManager.registerEvents(InventoryCloseListener(plugin), plugin)
        plugin.server.pluginManager.registerEvents(InventoryOpenListener(plugin), plugin)
    }
}