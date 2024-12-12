package me.justlime.redeemX.models

import java.time.LocalDateTime

data class RedeemCode(val code: String,
                      var commands: MutableMap<Int, String>,
                      var storedTime: LocalDateTime?,
                      var duration: String?,
                      var isEnabled: Boolean,
                      var maxRedeems: Int,
                      var maxPlayers: Int,
                      var permission: String?,
                      var pin: Int,
                      var target: MutableList<String?>,
                      var usage: MutableMap<String, Int>,
                      var template: String,
                      var templateLocked: Boolean,
                      var storedCooldown: LocalDateTime?,
                      var cooldown: String?
) {
    companion object {
        fun getEmpty(): RedeemCode {
            return RedeemCode(
                code = "",
                commands = mutableMapOf(),
                storedTime = null,
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