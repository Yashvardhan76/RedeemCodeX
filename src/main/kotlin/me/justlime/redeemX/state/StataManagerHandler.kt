package me.justlime.redeemX.state

import me.justlime.redeemX.data.models.RedeemCode
import org.bukkit.command.CommandSender

interface StateHandler {
    /**
     * The current redeem code being managed.
     */
    var redeemCode: RedeemCode

    /**
     * Retrieves the current state for the sender, creating a new state if one does not already exist.
     *
     * @param sender The command sender associated with the state.
     * @param code (Optional) A specific redeem code to associate with the state.
     * @return The existing or newly created [RedeemCodeState].
     */
    fun createState(sender: CommandSender, code: String? = null): RedeemCodeState

    fun getState(sender: CommandSender): RedeemCodeState


    /**
     * Updates the state for the sender with the provided redeem code data.
     *
     * @param sender The command sender associated with the state.
     * @param codeData The redeem code data to update the state with. Defaults to the current [redeemCode].
     */
    fun fetchState(state: RedeemCodeState): Boolean

    fun fetchStateByTemplate(sender: CommandSender,template: String): Boolean
    /**
     * Clears the state associated with the specified sender.
     *
     * @param sender The command sender whose state should be cleared.
     */
    fun clearState(sender: CommandSender)

    /**
     * Clears all stored states for all senders.
     */
    fun clearAllStates()

    fun updateState(state: RedeemCodeState): Boolean
    /**
     * Updates the underlying database to reflect the current state.
     */
    fun updateDb(sender: CommandSender): Boolean
}
