package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.enums.JTab
import me.justlime.redeemX.models.CodePlaceHolder

class UsageSubCommand(plugin: RedeemX) : JSubCommand {
    override var codeList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.USAGE
    val config = ConfigRepository(plugin)
    val codeRepo = RedeemCodeRepository(plugin)

    override fun execute(sender: org.bukkit.command.CommandSender, args: MutableList<String>): Boolean {
        var placeHolder = CodePlaceHolder(sender, args)
        if (!hasPermission(sender)) {
            config.sendMsg(JMessage.NO_PERMISSION, placeHolder)
            return false
        }

        if (args.size < 3) {
            config.sendMsg(JMessage.Commands.Help.UNKNOWN_COMMAND, CodePlaceHolder(sender))
            return false
        }
        val type = args[1]
        when (type){
            JTab.Type.Code.value -> {
                val redeemCode = codeRepo.getCode(args[2])
                if (redeemCode == null) {
                    config.sendMsg(JMessage.Commands.Usage.CODE_NOT_FOUND, placeHolder)
                    return false
                }
                placeHolder = CodePlaceHolder.applyByRedeemCode(redeemCode, sender)
                config.sendMsg(JMessage.Commands.Usage.CODE, placeHolder)
            }
            JTab.Type.Template.value -> {
                val template = config.getTemplate(args[2])
                if (template==null){
                    config.sendMsg(JMessage.Commands.Usage.TEMPLATE_NOT_FOUND, placeHolder)
                    return false
                }
                placeHolder = CodePlaceHolder.applyByTemplate(template, sender)
                config.sendMsg(JMessage.Commands.Usage.TEMPLATE, placeHolder)
            }
            else -> {
                config.sendMsg(JMessage.Commands.Usage.TEMPLATE, placeHolder)
                return false
            }
        }

        return true
    }
}