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
        val placeHolder = CodePlaceHolder(sender, args.toMutableList())
        if (sender !is Player) {
            config.sendMsg(JMessage.RESTRICTED_TO_PLAYERS, placeHolder)
            return true
        }

        if (args.isEmpty()) {
            config.sendMsg(JMessage.Redeemed.USAGE, placeHolder)
            return true
        }
        placeHolder.code = args[0]
        val codeValidation = CodeValidation(plugin, args[0])
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
        if (!codeValidation.isTargetValid(sender.name)) {
            config.sendMsg(JMessage.Redeemed.INVALID_TARGET, placeHolder)
            return true
        }

        if (codeValidation.isPinRequired()) {
            if (args.size < 2) {
                config.sendMsg(JMessage.Redeemed.MISSING_PIN, placeHolder)
                return true
            }

            val pin = args[1].toIntOrNull() ?: 0
            placeHolder.pin = pin.toString()
            if (codeValidation.isCorrectPin(pin)) {
                config.sendMsg(JMessage.Redeemed.INVALID_PIN, placeHolder)
                return true
            }
        }

        // Execute commands
        val console = plugin.server.consoleSender
        val code = codeValidation.code
        code.commands.values.forEach {
            plugin.server.dispatchCommand(console, it)
        }

        code.usage[sender.name] = (code.usage[sender.name]?.plus(1)) ?: 1
        codeRepo.setStoredCooldown(code)
        val success = codeRepo.upsertCode(code)
        if (!success) {
            config.sendMsg(JMessage.Redeemed.FAILED, placeHolder)
            return true
        }

        // Success message
        config.sendMsg(JMessage.Redeemed.SUCCESS, placeHolder)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): List<String> {
        return emptyList()
    }

}
