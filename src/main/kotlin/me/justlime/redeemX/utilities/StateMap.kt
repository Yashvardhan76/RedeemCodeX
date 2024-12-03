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
            template = redeemCode.template,
            storedCooldown = redeemCode.storedCooldown,
            cooldown = redeemCode.cooldown,
            isTemplateLocked = redeemCode.templateLocked,
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
            template = state.template,
            storedCooldown = state.storedCooldown,
            cooldown = state.cooldown,
            templateLocked = state.isTemplateLocked
        )
    }

    fun fetchState(state: RedeemCodeState, redeemCode: RedeemCode){
        state.apply {
            this.code = redeemCode.code
            this.inputCode = redeemCode.code
            this.inputTemplate = redeemCode.template
            this.template = redeemCode.template
            this.commands = redeemCode.commands
            this.storedTime = redeemCode.storedTime
            this.duration = redeemCode.duration
            this.isEnabled = redeemCode.isEnabled
            this.maxRedeems = redeemCode.maxRedeems
            this.maxPlayers = redeemCode.maxPlayers
            this.permission = redeemCode.permission
            this.target = redeemCode.target
            this.usage = redeemCode.usage
            this.storedCooldown = redeemCode.storedCooldown
            this.cooldown = redeemCode.cooldown
            this.isTemplateLocked = redeemCode.templateLocked
            state.pin = redeemCode.pin
        }


    }
}
