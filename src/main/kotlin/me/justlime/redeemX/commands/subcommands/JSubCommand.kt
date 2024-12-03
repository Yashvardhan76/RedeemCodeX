package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.state.RedeemCodeState

interface JSubCommand {
    fun execute(state: RedeemCodeState): Boolean
}
