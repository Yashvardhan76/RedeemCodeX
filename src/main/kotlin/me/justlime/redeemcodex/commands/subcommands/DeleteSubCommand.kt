/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 */


package me.justlime.redeemcodex.commands.subcommands

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.api.RedeemXAPI.deleteTemplate
import me.justlime.redeemcodex.commands.JSubCommand
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.data.repository.RedeemCodeRepository
import me.justlime.redeemcodex.enums.JFiles
import me.justlime.redeemcodex.enums.JMessage
import me.justlime.redeemcodex.enums.JPermission
import me.justlime.redeemcodex.enums.JTab
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.utilities.JLogger
import org.bukkit.command.CommandSender

class DeleteSubCommand(val plugin: RedeemCodeX) : JSubCommand {
    lateinit var placeHolder: CodePlaceHolder
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    override var jList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.DELETE

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        placeHolder = CodePlaceHolder(sender)
        if (!hasPermission(sender)) {
            sendMessage(JMessage.Command.NO_PERMISSION)
            return true
        }
        if (args.size < 3) {
            sendMessage(JMessage.Command.UNKNOWN_COMMAND)
            return true
        }

        when (args[1].lowercase()) {
            JTab.Type.CODE -> {
                val codes = mutableListOf(args[2].uppercase())

                if (codes.first() == JTab.Delete.All.value) {
                    if (args.size < 4 || args[3] != JTab.Delete.Confirm.value) return sendMessage(JMessage.Code.Delete.CONFIRMATION_NEEDED)
                    if (args.size < 4 || args[3] == JTab.Delete.Confirm.value) {
                        deleteAllCodes()
                        jList = mutableListOf(JTab.Delete.All.value)
                        codeRepo.fetch()
                        return true
                    }
                } else if (!args.getOrNull(3).isNullOrBlank()) {
                    codes.addAll(args.drop(3).map { it.uppercase() })
                    deleteCodes(codes, placeHolder)
                } else {
                    deleteCode(codes.first(), placeHolder)
                }
                jList = codes
                codeRepo.fetch()
                return true
            }

            JTab.Type.TEMPLATE -> {
                val templates = mutableListOf(args[2].uppercase())
                placeHolder.template = templates.first()
                if (templates.first() == JTab.Delete.All.value) {
                    if (args.size < 4 || args[3] != JTab.Delete.Confirm.value) return sendMessage(JMessage.Template.Delete.CONFIRMATION_NEEDED)
                    deleteAllTemplates()
                    jList = mutableListOf(JTab.Delete.All.value)
                    return true
                } else if (!args.getOrNull(3).isNullOrBlank()) {
                    templates.addAll(args.subList(3, args.size))
                    templates.forEach { deleteTemplate(it, CodePlaceHolder(sender)) }
                } else {
                    deleteTemplate(templates.first(), CodePlaceHolder(sender))
                }
                jList = templates
                config.reloadConfig(JFiles.TEMPLATE)
                return true
            }

            else -> {
                sendMessage(JMessage.Command.UNKNOWN_COMMAND)
                return true
            }
        }
    }

    override fun sendMessage(key: String): Boolean {
        placeHolder.sentMessage = config.getMessage(key, placeHolder)
        config.sendMsg(key, placeHolder)
        return true

    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        val cachedCodes = codeRepo.getCachedCode()
        val cachedTemplate = config.getAllTemplates().map { it.name }
        val completions = mutableListOf<String>()

        if (!hasPermission(sender)) return mutableListOf()
        when (args.size) {
            2 -> {
                completions.addAll(mutableListOf(JTab.Type.CODE, JTab.Type.TEMPLATE))
            }

            3 -> {
                if (args[1] == JTab.Type.CODE) completions.addAll(cachedCodes)
                if (args[1] == JTab.Type.TEMPLATE) completions.addAll(cachedTemplate)
                if (args[1] == JTab.Type.CODE || args[1] == JTab.Type.TEMPLATE) completions.add(JTab.Delete.All.value)
            }

            4 -> {
                if (args[1] == JTab.Type.CODE && args[2] != JTab.Delete.All.value) completions.addAll(cachedCodes.filter { it !in args })
                if (args[1] == JTab.Type.TEMPLATE && args[2] != JTab.Delete.All.value) completions.addAll(cachedTemplate.filter { it !in args })
            }

            else -> {
                if (args[1] == JTab.Type.CODE && args[2] != JTab.Delete.All.value) completions.addAll(cachedCodes.filter { it !in args })
                if (args[1] == JTab.Type.TEMPLATE && args[2] != JTab.Delete.All.value) completions.addAll(cachedTemplate.filter { it !in args })
            }
        }
        return completions
    }

    private fun deleteCode(code: String, placeHolder: CodePlaceHolder) {
        placeHolder.code = code
        if (codeRepo.deleteCode(code)) {
            JLogger(plugin).logDelete(code)
            sendMessage(JMessage.Code.Delete.SUCCESS)
        }
        else sendMessage(JMessage.Code.Delete.NOT_FOUND)
    }

    private fun deleteCodes(codes: List<String>, placeHolder: CodePlaceHolder) {
        placeHolder.code = codes.joinToString(" ")
        if (codeRepo.deleteCodes(codes)) {
            codes.forEach { JLogger(plugin).logDelete(it) }
            sendMessage(JMessage.Code.Delete.SUCCESS_CODES)
        }
        else sendMessage(JMessage.Code.Delete.NOT_FOUND_ALL)
    }

    private fun deleteAllCodes() {
        if (codeRepo.getCachedCode().isEmpty()) {
            sendMessage(JMessage.Code.Delete.NOT_FOUND_ALL)
            return
        }
        codeRepo.deleteAllCodes()
        JLogger(plugin).logDelete("Deleted All Codes")
        sendMessage(JMessage.Code.Delete.SUCCESS_ALL)
    }

    private fun deleteTemplate(template: String, placeHolder: CodePlaceHolder): Boolean {
        placeHolder.template = template
        if (template == "DEFAULT") {
            sendMessage(JMessage.Template.Delete.FAILED_DEFAULT)
            return false
        }
        if (config.getTemplate(template) == null) {
            sendMessage(JMessage.Template.Delete.NOT_FOUND)
            return false
        }
        config.deleteTemplate(template)
        JLogger(plugin).logDelete("$template (TEMPLATE)")
        sendMessage(JMessage.Template.Delete.SUCCESS)
        return true
    }

    private fun deleteAllTemplates(): Boolean {
        config.deleteAllTemplates()
        JLogger(plugin).logDelete("Deleted All Templates")
        sendMessage(JMessage.Template.Delete.SUCCESS_ALL)
        return true
    }


}

