package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.enums.JTab
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.utilities.JService
import org.bukkit.command.CommandSender

class UsageSubCommand(plugin: RedeemX) : JSubCommand {
    override var codeList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.USAGE
    val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        var placeHolder = CodePlaceHolder(sender, args)
        if (!hasPermission(sender)) {
            config.sendMsg(JMessage.NO_PERMISSION, placeHolder)
            return false
        }

        if (args.size < 3) {
            config.sendMsg(JMessage.RCX.Help.UNKNOWN_COMMAND, CodePlaceHolder(sender))
            return false
        }
        val type = args[1]
        when (type) {
            JTab.Type.Code.value -> {
                val redeemCode = codeRepo.getCode(args[2])
                if (redeemCode == null) {
                    config.sendMsg(JMessage.RCX.Usage.CODE_NOT_FOUND, placeHolder)
                    return false
                }
                placeHolder = CodePlaceHolder.applyByRedeemCode(redeemCode, sender)
                placeHolder.totalRedemption = redeemCode.usedBy.values.sum().toString()
                placeHolder.totalPlayerUsage = redeemCode.usedBy.size.toString()
                placeHolder.usedBy = redeemCode.usedBy.map {
                    "${it.key} = ${it.value}"
                }.joinToString(", ")
                val commandsBuilder = StringBuilder()
                redeemCode.commands.forEachIndexed { index, command ->
                    commandsBuilder.append("[$index] $command\n")
                }
                placeHolder.command = commandsBuilder.toString()
                if(placeHolder.pin == "none") placeHolder.pin = JService.applyColors("&cdisabled")
                if (placeHolder.permission == "") placeHolder.permission = JService.applyColors("&cdisabled")


                config.sendMsg(JMessage.RCX.Usage.CODE, placeHolder)
            }

            JTab.Type.Template.value -> {
                val template = config.getTemplate(args[2])
                if (template == null) {
                    config.sendMsg(JMessage.RCX.Usage.TEMPLATE_NOT_FOUND, placeHolder)
                    return false
                }
                placeHolder = CodePlaceHolder.applyByTemplate(template, sender)
                config.sendMsg(JMessage.RCX.Usage.TEMPLATE, placeHolder)
            }

            else -> {
                config.sendMsg(JMessage.RCX.Usage.TEMPLATE, placeHolder)
                return false
            }
        }

        return true
    }
}