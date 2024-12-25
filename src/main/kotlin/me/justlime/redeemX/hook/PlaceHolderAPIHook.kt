package me.justlime.redeemX.hook
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.justlime.redeemX.RedeemX
import org.bukkit.OfflinePlayer

class PlaceHolderAPIHook(redeemX: RedeemX) : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "redeemx"
    }

    override fun getAuthor(): String {
        return "JustLime"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun onRequest(player: OfflinePlayer?, identifier: String): String? {
        return when (identifier) {
            "test" -> "This is a test placeholder"
            else -> null
        }
    }

}