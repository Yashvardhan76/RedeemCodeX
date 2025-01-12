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


package me.justlime.redeemcodex.enums

enum class JProperty(val property: String) {
    CODE("code"),
    ENABLED("enabled"),
    TEMPLATE("template"),
    SYNC("sync"),
    DURATION("duration"),
    COOLDOWN("cooldown"),
    PERMISSION("permission"),
    PIN("pin"),
    REDEMPTION("redemption"),
    PLAYER_LIMIT("playerLimit"),
    USED_BY("usedBy"),
    VALID_FROM("validFrom"),
    LAST_REDEEMED("lastRedeemed"),
    TARGET("target"),
    COMMANDS("commands"),
    REWARDS("rewards"),
    Message("messages"),
    Sound("sound"),
    MODIFIED("modified") //Last Modified at
}