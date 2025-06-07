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

import org.bukkit.Sound
import org.bukkit.entity.Player
import java.io.Serializable


data class SoundState(
    var sound: Sound?,
    var volume: Float,
    var pitch: Float
): Serializable {
    private companion object {
        private const val serialVersionUID = 1L
    }
    fun playSound(player: Player) {
        player.playSound(player.location, sound ?: return, volume, pitch)
    }
}