package me.justlime.redeemX.models

import java.sql.Timestamp

data class RedeemCode(
    val code: String,
    var duration: String, //0s for disabled
    var cooldown: String, //0s for disabled
    var pin: Int, //0 for disabled
    var redemption: Int, //0 for infinite Redemption limit
    var limit: Int, //0 for infinite player limit
    var enabled: Boolean,
    var permission: String, //Blank for disabled
    var target: MutableList<String>, //Blank for disabled
    var template: String, //Blank for disabled
    var locked: Boolean,
    var validFrom: Timestamp,
    var lastRedeemed: MutableMap<String, Timestamp>,
    var usedBy: MutableMap<String, Int>,
    var commands: MutableMap<Int, String> //Empty list for disabled
)