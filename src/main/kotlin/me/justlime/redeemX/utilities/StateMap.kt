package me.justlime.redeemX.utilities

import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.state.RedeemCodeState
import org.bukkit.command.CommandSender

object StateMap {
    // Mapping from RedeemCode to RedeemCodeState
    fun toState(redeemCode: RedeemCode, sender: CommandSender): RedeemCodeState {
        return RedeemCodeState(
            sender = sender,
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
            templateName = redeemCode.template,
            storedCooldown = redeemCode.storedCooldown,
            cooldown = redeemCode.cooldown
        )
    }

    // Mapping from RedeemCodeState to RedeemCode
    fun toModel(state: RedeemCodeState): RedeemCode {
        return RedeemCode(
            code = state.code,
            commands = state.commands,
            storedTime = state.storedTime,
            duration = state.duration,
            isEnabled = state.isEnabled,
            maxRedeems = state.maxRedeems,
            maxPlayers = state.maxPlayers,
            permission = state.permission,
            pin = state.pin,
            target = state.target,
            usage = state.usage,
            template = state.templateName,
            storedCooldown = state.storedCooldown,
            cooldown = state.cooldown,
            templateLocked = state.templateLocked
        )
    }
}
