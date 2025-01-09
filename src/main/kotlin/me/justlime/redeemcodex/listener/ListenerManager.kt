package me.justlime.redeemcodex.listener

import me.justlime.redeemcodex.RedeemCodeX

class ListenerManager(plugin: RedeemCodeX) {
    val asyncPlayerChatListener = AsyncPlayerChatListener()

    init {
        plugin.server.pluginManager.registerEvents(InventoryClickListener(plugin), plugin)
        plugin.server.pluginManager.registerEvents(InventoryCloseListener(plugin), plugin)
        plugin.server.pluginManager.registerEvents(InventoryOpenListener(plugin), plugin)
        plugin.server.pluginManager.registerEvents(asyncPlayerChatListener, plugin)
    }
}