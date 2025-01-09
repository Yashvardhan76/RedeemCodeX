package me.justlime.redeemcodex.models

import org.bukkit.Sound
import org.bukkit.entity.Player
import java.io.Serializable

data class SoundState(
    val sound: Sound?,
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