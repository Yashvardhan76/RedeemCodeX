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

import me.justlime.redeemcodex.models.RedeemCode
import me.justlime.redeemcodex.models.RedeemTemplate

sealed class RedeemType {
    data class Code(val redeemCode: RedeemCode) : RedeemType()
    data class Template(val redeemTemplate: RedeemTemplate) : RedeemType()
}