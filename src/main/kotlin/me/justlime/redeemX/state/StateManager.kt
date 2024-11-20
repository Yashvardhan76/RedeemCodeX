package me.justlime.redeemX.state

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.utilities.StateMap
import org.bukkit.command.CommandSender
import java.util.concurrent.ConcurrentHashMap

class StateManager(val plugin: RedeemX) : StateManagerHandler {

    private val db = plugin.redeemCodeDB
    override lateinit var redeemCode: RedeemCode

    // A thread-safe map to store states for each sender
    private val stateMap: MutableMap<CommandSender, RedeemCodeState> = ConcurrentHashMap()

    /**
     * Gets the existing state for a sender or creates a new one.
     *
     * @param sender The command sender (Player or Console).
     * @param code Optional redeem code to initialize the state.
     * @return The existing or newly created RedeemCodeState.
     */
    override fun createState(sender: CommandSender, code: String?): RedeemCodeState {
        clearState(sender)
        return stateMap.computeIfAbsent(sender) {
            RedeemCodeState(sender = sender).apply {
                if (code != null) {
                    this.inputCode = code
                }
            }
        }
    }

    /**
     * Updates a state for a given sender based on a RedeemCode.
     *
     * @param sender The command sender (Player or Console).
     */
    override fun fetchState(sender: CommandSender,code: String): Boolean {
        val redeemCode = db.get(code) ?: return false
        stateMap[sender]?.let { state ->
            redeemCode.let {
                state.code = it.code
                state.commands = it.commands
                state.storedTime = it.storedTime
                state.duration = it.duration
                state.isEnabled = it.isEnabled
                state.maxRedeems = it.maxRedeems
                state.maxPlayers = it.maxPlayers
                state.permission = it.permission
                state.pin = it.pin
                state.target = it.target
                state.usage = it.usage
            }
        }
        return true
    }

    /**
     * Clears the state for a specific sender.
     *
     * @param sender The command sender whose state should be cleared.
     */
    override fun clearState(sender: CommandSender) {
        stateMap.remove(sender)
    }

    /**
     * Clears all states. Useful during plugin reloads or shutdowns.
     */
    override fun clearAllStates() {
        stateMap.clear()
    }


    override fun updateDb(sender: CommandSender): Boolean {
        val state = stateMap[sender] ?: return false
        val redeemCode = state.let { StateMap.toModel(it) }
        try {
            db.upsert(redeemCode)

            return true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to update database: ${e.message}")
            return false
        }
    }
}
