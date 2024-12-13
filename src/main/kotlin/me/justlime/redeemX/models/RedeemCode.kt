package me.justlime.redeemX.models

import me.justlime.redeemX.utilities.RedeemCodeService
import java.sql.Timestamp

data class RedeemCode(
    val code: String,
    var commands: MutableMap<Int, String>, //Empty list for disabled
    var storedTime: Timestamp,
    var duration: String, //0s for disabled
    var isEnabled: Boolean,
    var maxRedeems: Int,
    var maxPlayers: Int,
    var permission: String, //Blank for disabled
    var pin: Int, //0 for disabled
    var target: MutableList<String>, //Blank for disabled
    var usage: MutableMap<String, Int>,
    var template: String, //Blank for disabled
    var templateLocked: Boolean,
    var storedCooldown: Timestamp,
    var cooldown: String //0s for disabled
) {
    companion object {
        fun getEmpty(): RedeemCode {
            return RedeemCode(
                code = "",
                commands = mutableMapOf(),
                storedTime = RedeemCodeService().currentTime,
                duration = "0s",
                isEnabled = false,
                maxRedeems = 1,
                maxPlayers = 1,
                permission = "",
                pin = 0,
                target = mutableListOf(),
                usage = mutableMapOf(),
                template = "",
                templateLocked = false,
                storedCooldown = RedeemCodeService().currentTime,
                cooldown = "0s"
            )
        }
    }
}