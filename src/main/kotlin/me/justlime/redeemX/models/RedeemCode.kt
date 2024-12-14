package me.justlime.redeemX.models

import java.sql.Timestamp

data class RedeemCode(
    val code: String,
    var commands: MutableMap<Int, String>, //Empty list for disabled
    var storedTime: Timestamp,
    var duration: String, //0s for disabled
    var isEnabled: Boolean,
    var maxRedeems: Int, //0 for infinite Redemption limit
    var maxPlayers: Int, //0 for infinite player limit
    var permission: String, //Blank for disabled
    var pin: Int, //0 for disabled
    var target: MutableList<String>, //Blank for disabled
    var usage: MutableMap<String, Int>,
    var template: String, //Blank for disabled
    var templateLocked: Boolean,
    var storedCooldown: Timestamp,
    var cooldown: String //0s for disabled
)