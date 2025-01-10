package me.justlime.redeemcodex.listener

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AsyncPlayerChatListener : Listener {

    private val playerCallbacks = ConcurrentHashMap<Player, (String) -> Unit>()
    private val timeoutTasks = ConcurrentHashMap<Player, Runnable>()
    private val scheduler = Executors.newScheduledThreadPool(1)

    /**
     * Registers a callback for a player's chat input.
     * Replaces any existing callback for the same player.
     */
    fun registerCallback(player: Player, timeout: Long? = null, onTimeout: (() -> Unit)? = null, callback: (String) -> Unit) {
        unregisterCallback(player) // Clear any existing callback
        playerCallbacks[player] = callback

        if (timeout != null && onTimeout != null) {
            val timeoutTask = Runnable {
                if (playerCallbacks.remove(player) != null) {
                    onTimeout()
                }
            }
            timeoutTasks[player] = timeoutTask
            scheduler.schedule(timeoutTask, timeout, TimeUnit.SECONDS)
        }
    }

    /**
     * Unregisters a callback for a player's chat input.
     */
    fun unregisterCallback(player: Player) {
        playerCallbacks.remove(player)
        timeoutTasks.remove(player)
    }

    /**
     * Event handler for player chat input.
     */
    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val callback = playerCallbacks[player] ?: return

        event.isCancelled = true // Prevent other plugins from handling this message
        callback(event.message) // Process the input with the callback

    }
}
