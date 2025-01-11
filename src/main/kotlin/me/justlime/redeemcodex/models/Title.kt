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

package me.justlime.redeemcodex.models

data class Title(
    val title: String = "",
    val subTitle: String = "",
    val fadeIn: Int = 10,
    val stay: Int = 70,
    val fadeOut: Int = 20
)