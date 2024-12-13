package me.justlime.redeemX.commands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.yml.JMessage
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class RedeemCommand(private val plugin: RedeemX) : CommandExecutor, TabCompleter {

    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val placeHolder = CodePlaceHolder(sender,args.toMutableList())
        if (sender !is Player) {
            config.sendMsg(JMessage.RESTRICTED_TO_PLAYERS, placeHolder)
            return true
        }

        if (args.isEmpty()) {
            config.sendMsg(JMessage.Redeemed.USAGE, placeHolder)
            return true
        }
        val code = codeRepo.getCode(args[0])
        placeHolder.code = args[0]
        val codeValidation = CodeValidation(plugin,args[0])
        if (!codeValidation.isValidCode(args[0]) || code == null) {
            config.sendMsg(JMessage.Redeemed.INVALID_CODE, placeHolder)
            return true
        }
        if (!codeValidation.isCodeExist()) {
            config.sendMsg(JMessage.Redeemed.INVALID_CODE, placeHolder)
            return true
        }

        if (codeValidation.isReachedMaximumRedeem()) {
            config.sendMsg(JMessage.Redeemed.MAX_REDEMPTIONS, placeHolder)
            return true
        }

        if (codeValidation.isReachedMaximumPlayer()) {
            config.sendMsg(JMessage.Redeemed.MAX_PLAYER_REDEEMED, placeHolder)
            return true
        }

        if (!codeValidation.hasPermission(sender)) {
            config.sendMsg(JMessage.Redeemed.NO_PERMISSION, placeHolder)
            return true
        }

        if (!codeValidation.isCodeEnabled()) {
            config.sendMsg(JMessage.Redeemed.DISABLED, placeHolder)
            return true
        }

        if (codeValidation.isCodeExpired()) {
            config.sendMsg(JMessage.Redeemed.EXPIRED_CODE, placeHolder)
            return true
        }

        // Target validation
        val tempString = code.target.toString().removeSurrounding("[", "]").trim()
        if (tempString.isNotBlank()) {
            val temp: MutableList<String> = mutableListOf()
            code.target.filterNotNull().toMutableList().forEach {
                temp.add(it.trim())
            }
            code.target = temp
            if (!code.target.contains(sender.name)) {
                config.sendMsg(JMessage.Redeemed.INVALID_TARGET, placeHolder)
                return true
            }
        }

        if (code.pin >= 0) {
            if (args.size < 2) {
                config.sendMsg(JMessage.Redeemed.MISSING_PIN, placeHolder)
                return true
            }

            val inputPin = args[1].toIntOrNull()
            if (inputPin != code.pin) {
                config.sendMsg(JMessage.Redeemed.INVALID_PIN, placeHolder)
                return true
            }
        }

        // Execute commands
        val console = plugin.server.consoleSender
        code.commands.values.forEach {
            plugin.server.dispatchCommand(console, it)
        }

        code.usage[sender.name] = (code.usage[sender.name]?.plus(1)) ?: 1
        val success = codeRepo.upsertCode(code)
        if (!success) {
            config.sendMsg(JMessage.Redeemed.FAILED, placeHolder)
            return true
        }

        // Success message
        config.sendMsg(JMessage.Redeemed.SUCCESS, placeHolder)
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): List<String> {
        return emptyList()
    }

}
