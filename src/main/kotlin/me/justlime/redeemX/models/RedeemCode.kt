package me.justlime.redeemX.models

import java.sql.Timestamp

data class RedeemCode(
    val code: String,
    var commands: MutableMap<Int, String>,
    var storedTime: Timestamp,
    var duration: String?,
    var isEnabled: Boolean,
    var maxRedeems: Int,
    var maxPlayers: Int,
    var permission: String?,
    var pin: Int,
    var target: MutableList<String>,
    var usage: MutableMap<String, Int>,
    var template: String,
    var templateLocked: Boolean,
    var storedCooldown: Timestamp?,
    var cooldown: String?
) {
    companion object {
        fun getEmpty(): RedeemCode {
            return RedeemCode(
                code = "",
                commands = mutableMapOf(),
                storedTime = Timestamp(System.currentTimeMillis()),
                duration = null,
                isEnabled = false,
                maxRedeems = 1,
                maxPlayers = 1,
                permission = null,
                pin = 0,
                target = mutableListOf(),
                usage = mutableMapOf(),
                template = "",
                templateLocked = false,
                storedCooldown = null,
                cooldown = null
            )
        }
    }
}