package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

class DeleteSubCommand(private val plugin: RedeemX) {
    fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2 && args[0].equals("delete", ignoreCase = true)) {
            sender.sendMessage("Usage: /rxc delete <code>")
            return
        }
        if (args.size < 2 || args[0].equals("delete_all", ignoreCase = true)) {
            sender.sendMessage("${ChatColor.YELLOW}Usage: /rxc delete_all CONFIRM")
            return
        }
        when (args[0].lowercase()) {
            "delete" -> {
                val codeToDelete = args[1]
                val redeemCode = plugin.redeemCodeDB.get(codeToDelete)

                if (redeemCode == null) {
                    sender.sendMessage("The code '$codeToDelete' does not exist.")
                    return
                }
                val success = plugin.redeemCodeDB.deleteByCode(codeToDelete)
                if (!success) {
                    sender.sendMessage("${ChatColor.RED}Failed to delete the code: ${redeemCode.code}")
                    return
                }
                sender.sendMessage("${ChatColor.GREEN}Successfully deleted the code: ${redeemCode.code}")
                return
            }

            "delete_all" -> {
                val success = plugin.redeemCodeDB.deleteAll()
                if (!success) {
                    sender.sendMessage("${ChatColor.RED}Failed to delete all codes from the database.")
                    return
                }
                sender.sendMessage("${ChatColor.GREEN}Successfully deleted all codes from the database.")
                return
            }
        }
    }
}
