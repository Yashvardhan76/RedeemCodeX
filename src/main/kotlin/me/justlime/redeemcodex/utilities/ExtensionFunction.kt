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

package me.justlime.redeemcodex.utilities

fun List<String>.toIndexedMap(): MutableMap<Int, String> {
    return mapIndexed { index, value -> index to value }.toMap().toMutableMap()
}
