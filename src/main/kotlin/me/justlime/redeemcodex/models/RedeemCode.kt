/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 *
 */

package me.justlime.redeemcodex.models

import org.bukkit.inventory.ItemStack
import java.sql.Timestamp

data class RedeemCode(
    val code: String, var enabledStatus: Boolean,

    var template: String, //Blank for disabled
    var sync: Boolean,

    var duration: String, //0s for disabled
    var cooldown: String, //0s for disabled

    var permission: String, //Blank for disabled
    var pin: Int, //0 for disabled

    var redemption: Int, //0 for infinite Redemption limit
    var playerLimit: Int, //0 for infinite player limit

    var usedBy: MutableMap<String, Int>,

    var validFrom: Timestamp, var lastRedeemed: MutableMap<String, Timestamp>,

    var target: MutableList<String>, //Blank for disabled
    var commands: MutableList<String>, //Empty list for disabled

    var rewards: MutableList<ItemStack> = mutableListOf(),
    var messages: MessageState,
    var sound: SoundState,
    var modified: Timestamp
)