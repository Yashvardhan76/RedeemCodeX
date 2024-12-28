package me.justlime.redeemX.models

import org.bukkit.inventory.ItemStack
import java.sql.Timestamp

data class RedeemCode(
    val code: String,
    var enabled: Boolean,

    var template: String, //Blank for disabled
    var locked: Boolean,

    var duration: String, //0s for disabled
    var cooldown: String, //0s for disabled

    var permission: String, //Blank for disabled
    var pin: Int, //0 for disabled

    var redemption: Int, //0 for infinite Redemption limit
    var playerLimit: Int, //0 for infinite player limit

    var usedBy: MutableMap<String, Int>,
    var validFrom: Timestamp,
    var lastRedeemed: MutableMap<String, Timestamp>,
    var target: MutableList<String>, //Blank for disabled
    var commands: MutableList<String>, //Empty list for disabled

    var rewards: MutableList<ItemStack> = mutableListOf(),
    var messages: String = "",
    var sound: String = "",

    var modified: Timestamp
)