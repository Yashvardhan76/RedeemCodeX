package me.justlime.redeemX.utilities

import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.state.RedeemCodeState
import org.bukkit.command.CommandSender

object StateMap {
    // Mapping from RedeemCode to RedeemCodeState
    fun toState(redeemCode: RedeemCode, sender: CommandSender): RedeemCodeState {
        return RedeemCodeState(
            code = redeemCode.code,
            commands = redeemCode.commands,
            storedTime = redeemCode.storedTime,
            duration = redeemCode.duration,
            isEnabled = redeemCode.isEnabled,
            maxRedeems = redeemCode.maxRedeems,
            maxPlayers = redeemCode.maxPlayers,
            permission = redeemCode.permission,
            pin = redeemCode.pin,
            target = redeemCode.target,
            usage = redeemCode.usage,
            sender = sender,
            inputCode = "",
            inputCommand = "",
            inputStoredTime = "",
            inputDuration = "",
            inputEnabled = false.toString(),
            inputMaxRedeems = "",
            inputMaxPlayers = "",
            inputPermission = "",
            hasPermission = false,
            inputPin = -1,
            inputTarget = "",
            usageCount = 0
        )
    }

    // Mapping from RedeemCodeState to RedeemCode
    fun toModel(state: RedeemCodeState): RedeemCode {
        return RedeemCode(
            code = state.code,
            commands = state.commands,
            storedTime = state.storedTime, // Use lastAccessed as a substitute for storedTime in this case
            duration = null, // This is application-dependent; fill in if necessary
            isEnabled = true, // Set based on your application logic
            maxRedeems = 0, // Example, set based on application logic
            maxPlayers = 0, // Example
            permission = null, // Example
            pin = 0, // Example
            target = null, // Example
            usage = state.usage
        )
    }
}
