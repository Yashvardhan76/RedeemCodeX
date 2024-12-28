package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.CommandManager
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.enums.JTab
import me.justlime.redeemX.models.CodePlaceHolder
import org.bukkit.command.CommandSender

class DeleteSubCommand(private val plugin: RedeemX) : JSubCommand {
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    override var codeList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.DELETE

    private fun deleteCode(code: String, placeHolder: CodePlaceHolder) {
        if (codeRepo.deleteCode(code)) config.sendMsg(JMessage.RCX.Delete.Success.CODES, placeHolder)
        else config.sendMsg(JMessage.RCX.Delete.NotFound.CODES, placeHolder)
    }

    private fun deleteCodes(codes: List<String>, placeHolder: CodePlaceHolder) {
        if (codeRepo.deleteCodes(codes)) config.sendMsg(JMessage.RCX.Delete.Success.CODES, placeHolder)
        else config.sendMsg(JMessage.RCX.Delete.NotFound.CODES, placeHolder)
    }

    private fun deleteAllCodes(placeHolder: CodePlaceHolder) {
        codeRepo.deleteAllCodes()
        config.sendMsg(JMessage.RCX.Delete.Success.ALL, placeHolder)
    }

    private fun deleteTemplate(template: String, placeHolder: CodePlaceHolder): Boolean {
        if (template == "default") {
            config.sendMsg(JMessage.RCX.DeleteTemplate.FAILED, placeHolder)
            return false
        }
        if (config.getTemplate(template) == null) {
            config.sendMsg(JMessage.RCX.DeleteTemplate.NOT_FOUND, placeHolder)
            return false
        }
        config.deleteTemplate(template)
        config.sendMsg(JMessage.RCX.DeleteTemplate.SUCCESS, placeHolder)
        return true
    }

    private fun deleteAllTemplates(template: String,placeHolder: CodePlaceHolder): Boolean {
        config.deleteAllTemplates()
        config.sendMsg(JMessage.RCX.DeleteTemplate.SUCCESS_ALL, placeHolder)
        return true
    }

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)){
            config.sendMsg(JMessage.NO_PERMISSION, placeHolder)
            return true
        }
        if (args.size < 3) {
            config.sendMsg(JMessage.RCX.Help.UNKNOWN_COMMAND, placeHolder)
            return true
        }

        when (args[1].lowercase()) {
            JTab.Type.Code.value -> if (args[2] == JTab.Delete.All.value) {
                if (args.size < 4 || args[3] != JTab.Delete.Confirm.value) return config.sendMsg(
                    JMessage.RCX.Delete.CONFIRMATION_NEEDED, placeHolder
                ) != Unit
                if (args.size < 4 || args[3] == JTab.Delete.Confirm.value) {
                    deleteAllCodes(placeHolder)
                    codeList = mutableListOf("*")
                    return true
                }
                if (!args.getOrNull(3).isNullOrBlank()) {
                    val codes = args.subList(2, args.size)
                    placeHolder.code = codes.joinToString(" ")
                    deleteCodes(codes, placeHolder)
                    codeList = codes
                    return true
                }
                val code = args[2]
                placeHolder.code = code
                deleteCode(code, placeHolder)
                codeList = mutableListOf(code)
                return true
            }

            JTab.Type.Template.value -> {
                val template = args[2]
                placeHolder.template = template
                deleteTemplate(template, placeHolder)
            }
        }

        CommandManager(plugin).tabCompleterList.fetched()
        return true
    }
}

