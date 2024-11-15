package me.justlime.redeemX.state

import me.justlime.redeemX.data.models.RedeemCode
import org.bukkit.command.CommandSender
import java.util.concurrent.ConcurrentHashMap

class StateManager {

    // A thread-safe map to store states for each sender
    private val stateMap: MutableMap<CommandSender, RedeemCodeState> = ConcurrentHashMap()

    /**
     * Gets the existing state for a sender or creates a new one.
     *
     * @param sender The command sender (Player or Console).
     * @param code Optional redeem code to initialize the state.
     * @return The existing or newly created RedeemCodeState.
     */
    fun getOrCreateState(sender: CommandSender, code: String? = null): RedeemCodeState {
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
     * @param codeData The RedeemCode data to update the state with.
     */
    fun updateState(sender: CommandSender, codeData: RedeemCode?) {
        stateMap[sender]?.let { state ->
            codeData?.let {
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
    }

    /**
     * Clears the state for a specific sender.
     *
     * @param sender The command sender whose state should be cleared.
     */
    fun clearState(sender: CommandSender) {
        stateMap.remove(sender)
    }

    /**
     * Clears all states. Useful during plugin reloads or shutdowns.
     */
    fun clearAllStates() {
        stateMap.clear()
    }
}
