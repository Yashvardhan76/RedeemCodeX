/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *  This file handles the core logic for redeeming codes and managing associated data.
 *
 */

package me.justlime.redeemcodex.enums

enum class JTemplate(val property: String) {
    TEMPLATE_LOCKED("locked"),
    DURATION("duration"),
    COOLDOWN("cooldown"),
    PIN("pin"),
    MAX_REDEEMS("redemption"),
    MAX_PLAYERS("limit"),
    PERMISSION_REQUIRED("permission.required"),
    PERMISSION_VALUE("permission.value"),
    CODE_GENERATE_DIGIT("digit"),
    COMMAND("commands"),
}
