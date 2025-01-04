package me.justlime.redeemcodex.commands.subcommands

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.commands.JSubCommand
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.data.repository.RedeemCodeRepository
import me.justlime.redeemcodex.enums.JMessage
import me.justlime.redeemcodex.enums.JPermission
import me.justlime.redeemcodex.enums.JTab
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.command.CommandSender

class UsageSubCommand(plugin: RedeemCodeX) : JSubCommand {
    lateinit var placeHolder: CodePlaceHolder
    override var jList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.USAGE
    val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        placeHolder = CodePlaceHolder(sender, args)
        if (!hasPermission(sender)) {
            sendMessage(JMessage.Command.NO_PERMISSION)
            return false
        }

        if (args.size < 3) {
            sendMessage(JMessage.Command.UNKNOWN_COMMAND)
            return false
        }
        val type = args[1]
        fun commandPrinter(commands: List<String>): String{
            val commandsBuilder = StringBuilder()
            commands.forEachIndexed { index, command ->
                commandsBuilder.append("[$index] $command\n")
            }
            return commandsBuilder.toString()
        }
        when (type) {
            JTab.Type.CODE -> {
                val redeemCode = codeRepo.getCode(args[2])
                if (redeemCode == null) {
                    sendMessage(JMessage.Code.NOT_FOUND)
                    return false
                }
                placeHolder = CodePlaceHolder.applyByRedeemCode(redeemCode, sender)
                placeHolder.totalRedemption = redeemCode.usedBy.values.sum().toString()
                placeHolder.totalPlayerUsage = redeemCode.usedBy.size.toString()
                placeHolder.usedBy = redeemCode.usedBy.map {
                    "${it.key} = ${it.value}"
                }.joinToString(", ")
                placeHolder.target = redeemCode.target.joinToString(", ")
                placeHolder.command = commandPrinter(redeemCode.commands)
                if(placeHolder.pin == "none") placeHolder.pin = config.getMessage(JMessage.Code.Placeholder.DISABLED,placeHolder)
                if (placeHolder.permission == "") placeHolder.permission = JService.applyColors(config.getMessage(JMessage.Code.Placeholder.DISABLED,
                    placeHolder))

                sendMessage(JMessage.Code.Usages.USAGE)
            }

            JTab.Type.TEMPLATE -> {
                val template = config.getTemplate(args[2])
                if (template == null) {
                    sendMessage(JMessage.Template.NOT_FOUND)
                    return false
                }
                placeHolder = CodePlaceHolder.applyByTemplate(template, sender)
                placeHolder.command = commandPrinter(template.commands)
                if(placeHolder.pin == "none") placeHolder.pin = config.getMessage(JMessage.Code.Placeholder.DISABLED,placeHolder)
                if (placeHolder.permission == "") placeHolder.permission = JService.applyColors(config.getMessage(JMessage.Code.Placeholder.DISABLED,
                    placeHolder))

                sendMessage(JMessage.Template.USAGE)
            }

            else -> {
                sendMessage(JMessage.Template.USAGE)
                return false
            }
        }

        return true
    }

    override fun sendMessage(key: String): Boolean {
        placeHolder.sentMessage = config.getMessage(key, placeHolder)
        config.sendMsg(key, placeHolder)
        return true
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        val cachedCodes = codeRepo.getCachedCode()
        val cachedTemplate = config.getAllTemplates().map { it.name }
        return when (args.size) {
            2 -> mutableListOf(JTab.Type.CODE, JTab.Type.TEMPLATE)
            3 -> {
                val list = mutableListOf<String>()
                if (args[1] == JTab.Type.CODE) list.addAll(cachedCodes)
                if (args[1] == JTab.Type.TEMPLATE) list.addAll(cachedTemplate)
                list
            }
            else -> mutableListOf()
        }
    }
}