package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.models.RedeemCode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class RedeemCommand(private val plugin: RedeemX) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /rxc <gen|delete|modify|info>")
            return true
        }

        when (args[0].lowercase(Locale.getDefault())) {
            "gen" -> handleGenerate(sender, args)
            "delete" -> handleDelete(sender, args)
            "modify" -> handleModify(sender, args)
            "info" -> handleInfo(sender)
        }
        return true
    }

    private fun handleGenerate(sender: CommandSender, args: Array<out String>) {
        if (sender is Player && args.size > 2) {
            val commands = listOf(args[1])
            val codeName = args[2]
            val redeemCode = RedeemCode(
                code = codeName,
                commands = commands,
                maxRedeems = 10,
                maxPerPlayer = 1,
                isEnabled = true,
                expiry = null,
                permission = null,
                secureCode = null,
                specificPlayerId = null,
                guiEditMode = false
            )
            plugin.redeemCodeDao.insert(redeemCode)
            sender.sendMessage("Code generated: $codeName")
        } else {
            sender.sendMessage("Usage: /rxc gen <commands/template> <codesize/customName>")
        }
    }

    private fun handleDelete(sender: CommandSender, args: Array<out String>) {
        // Implement deletion logic
    }

    private fun handleModify(sender: CommandSender, args: Array<out String>) {
        // Implement modification logic
    }

    private fun handleInfo(sender: CommandSender) {
        sender.sendMessage("RedeemX Plugin Version: ${plugin.description.version}")
    }
}