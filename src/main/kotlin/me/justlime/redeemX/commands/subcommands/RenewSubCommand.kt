package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import org.bukkit.command.CommandSender
import java.time.LocalDateTime
import java.time.ZoneId

class RenewSubCommand(private val plugin: RedeemX) {
    fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("Usage: /rxc renew <code>")
            return
        }
        val code = args[1]
        val redeemCode = plugin.redeemCodeDB.get(code)

        if (redeemCode == null) {
            sender.sendMessage("The code '$code' does not exist.")
            return
        }

        redeemCode.usage.clear()

        val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
        val currentTime: LocalDateTime = LocalDateTime.now(timeZoneId)
        if (redeemCode.storedTime != null) redeemCode.storedTime = currentTime

        val success = plugin.redeemCodeDB.upsert(redeemCode)

        if (!success) {
            sender.sendMessage("Failed to renew the code: ${redeemCode.code}")
            return
        }
        sender.sendMessage("Successfully renewed the code: ${redeemCode.code}")
    }
}