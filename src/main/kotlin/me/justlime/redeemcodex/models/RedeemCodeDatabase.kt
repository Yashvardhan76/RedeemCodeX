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

import java.sql.Timestamp

data class RedeemCodeDatabase(
    val code: String,
    val enabled: Boolean,
    val template: String,
    val sync: Boolean,
    val duration: String,
    val cooldown: String,
    val permission: String,
    val pin: Int,
    val redemption: Int,
    val playerLimit: Int,
    val usedBy: String,
    val validFrom: Timestamp,
    val lastRedeemed: String,
    val target: String,
    val commands: String,
    val rewards: String,
    val messages: String,
    val sound: String,
    val created_at: Timestamp = Timestamp(System.currentTimeMillis()),
    val last_modified: Timestamp = Timestamp(System.currentTimeMillis())
)