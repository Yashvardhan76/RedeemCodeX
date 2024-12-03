package me.justlime.redeemX.state

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.utilities.StateMap
import org.bukkit.command.CommandSender
import java.util.concurrent.ConcurrentHashMap

class StateManager(val plugin: RedeemX) : StateHandler {

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

    override fun getState(sender: CommandSender): RedeemCodeState {
        return stateMap[sender] ?: createState(sender)
    }

    /**
     * Updates a state for a given sender based on a RedeemCode.
     *
     * @param sender The command sender (Player or Console).
     */
    override fun fetchState(state: RedeemCodeState): Boolean {
        val sender = state.sender
        val code = state.inputCode
        val redeemCode = db.get(code) ?: return false
        stateMap[sender] = StateMap.toState(redeemCode, sender)
        StateMap.fetchState(state, redeemCode)
        return true
    }

    override fun fetchStateByTemplate(sender: CommandSender, template: String): Boolean {
        val redeemCode = db.getTemplateCodes(template) ?: return false
//        stateMap[sender]?.let { redeemCode.let { StateMap.toState(it, sender) } }
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

    override fun updateState(state: RedeemCodeState): Boolean {
        val sender = state.sender
        stateMap[sender] = state
        return true
    }

    override fun updateDb(sender: CommandSender): Boolean {
        val state = stateMap[sender] ?: return false
        val redeemCode = StateMap.toModel(state)
        try {
            db.upsert(redeemCode)

            return true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to update database: ${e.message}")
            return false
        }
    }
}
