/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 *
 */


package me.justlime.redeemcodex.commands.subcommands

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.commands.JSubCommand
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.data.repository.RedeemCodeRepository
import me.justlime.redeemcodex.enums.JConfig
import me.justlime.redeemcodex.enums.JMessage
import me.justlime.redeemcodex.enums.JPermission
import me.justlime.redeemcodex.enums.JTab
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.models.RedeemCode
import me.justlime.redeemcodex.models.RedeemTemplate
import me.justlime.redeemcodex.models.SoundState
import me.justlime.redeemcodex.utilities.JLogger
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.Sound
import org.bukkit.command.CommandSender

class GenerateSubCommand(private val plugin: RedeemCodeX) : JSubCommand {
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    private val generatedCodesList = mutableListOf<String>()
    override var jList: List<String> = generatedCodesList
    override val permission: String = JPermission.Admin.GEN
    lateinit var sender: CommandSender
    lateinit var placeHolder: CodePlaceHolder
    private var isDefaultLoaded = false
    private var usingDefault = false

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        this.sender = sender
        placeHolder = CodePlaceHolder(sender, args)
        // Validate minimum arguments
        if (!hasPermission(sender)) {
            sendMessage(JMessage.Command.NO_PERMISSION)
            return true
        }
        if (args.size < 3) {
            sendMessage(JMessage.Command.UNKNOWN_COMMAND)
            return false
        }

        val type = args[1].lowercase()
        val digit = args[2].toIntOrNull()
        val amount = if (digit != null) args.getOrNull(4)?.toIntOrNull() ?: 1 else 1

        if (args[1] == JTab.Type.CODE && amount >= 1) {
            val codes = mutableListOf<RedeemCode>()
            for (index in 1..amount) {
                if (digit != null) {
                    handleNumericGeneration(digit, args.getOrNull(3) ?: "DEFAULT")?.let { codes.add(it) } ?: break
                } else {
                    handleCodeCreation(args[2], args.getOrNull(3) ?: "DEFAULT")?.let { codes.add(it) } ?: return true
                }
            }
            if (isDefaultLoaded) sendMessage(JMessage.Code.Generate.MISSING)
            if (codes.size == 1) {
                upsertRedeemCode(codes[0])
            } else upsertRedeemCodes(codes)

//            CommandManager(plugin).tabCompleterList.fetched()

            // Reset
            usingDefault = false
            isDefaultLoaded = false

            return true
        }
        if (args[1] == JTab.Type.CODE && amount < 1) sendMessage(JMessage.Code.Generate.INVALID_AMOUNT)
        if (type == JTab.Type.TEMPLATE) {
            generateTemplate(args[2].uppercase())
//            CommandManager(plugin).tabCompleterList.fetched()
            if (isDefaultLoaded) sendMessage(JMessage.Code.Generate.MISSING)
            // Reset
            isDefaultLoaded = false

            return true
        }
        sendMessage(JMessage.Command.UNKNOWN_COMMAND)
        return false
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        val cachedTemplate = config.getAllTemplates().map { it.name }
        val completions = mutableListOf<String>()

        if (!hasPermission(sender)) return mutableListOf()
        when (args.size) {
            2 -> {
                completions.addAll(mutableListOf(JTab.Type.CODE, JTab.Type.TEMPLATE))
            }

            3 -> {
                if (args[1] == JTab.Type.CODE) completions.addAll(mutableListOf(JTab.Generate.CUSTOM, JTab.Generate.DIGIT))
                if (args[1] == JTab.Type.TEMPLATE) completions.add(JTab.Generate.TEMPLATE_NAME)
            }

            4 -> {
                if (args[1] == JTab.Type.CODE) completions.addAll(cachedTemplate)
            }

            5 -> {
                if (args[1] == JTab.Type.CODE && args[2].toIntOrNull() != null) completions.addAll(mutableListOf(JTab.Generate.AMOUNT))
            }
        }
        return completions
    }

    override fun sendMessage(key: String): Boolean {
        placeHolder.sentMessage = config.getMessage(key, placeHolder)
        config.sendMsg(key, placeHolder)
        return true
    }

    private fun generateTemplate(templateName: String): Boolean {
        placeHolder.template = templateName
        if (config.getTemplate(templateName) != null) return !sendMessage(JMessage.Template.Generate.ALREADY_EXIST)
        val template = config.loadDefaultTemplateValues(templateName)
        template.defaultSync = true
        config.createTemplate(template)
        JLogger(plugin).logGenerate("$templateName (TEMPLATE)")
        sendMessage(JMessage.Template.Generate.SUCCESS)
        return true
    }

    private fun handleNumericGeneration(codeLength: Int, templateName: String): RedeemCode? {
        placeHolder.template = templateName
        val (minLength, maxLength) = config.getCodeLengthRange(placeHolder)
        if (codeLength !in minLength..maxLength) {
            sendMessage(JMessage.Code.Generate.INVALID_RANGE)
            return null
        }
        val uniqueCode = generateCode(codeLength)
        if (uniqueCode == null) {
            sendMessage(JMessage.Code.Generate.INVALID_LENGTH)
            return null
        }
        placeHolder.code = uniqueCode
        val template = config.getTemplate(templateName) ?: return null
        return createRedeemCode(uniqueCode, template)
    }

    private fun handleCodeCreation(code: String, templateName: String): RedeemCode? {
        placeHolder.template = templateName
        placeHolder.code = code.uppercase()

        if (codeRepo.getCode(code.uppercase()) != null) {
            sendMessage(JMessage.Code.Generate.ALREADY_EXIST)
            return null
        }

        val template = config.getTemplate(templateName) ?: return null
        val redeemCode = createRedeemCode(code.uppercase(), template)
        return redeemCode
    }

    private fun createRedeemCode(code: String, redeemTemplate: RedeemTemplate): RedeemCode = RedeemCode(
        code = code.uppercase(),
        template = redeemTemplate.name,
        commands = redeemTemplate.commands,
        validFrom = JService.getCurrentTime(),
        duration = redeemTemplate.duration,
        enabledStatus = true,
        redemption = redeemTemplate.redemption,
        playerLimit = redeemTemplate.playerLimit,
        permission = if (redeemTemplate.permissionRequired) redeemTemplate.permissionValue.replace("{code}", code.lowercase()) else "",
        pin = redeemTemplate.pin,
        sync = redeemTemplate.defaultSync,
        usedBy = mutableMapOf(),
        target = mutableListOf(),
        lastRedeemed = mutableMapOf(),
        cooldown = "0s",
        modified = JService.getCurrentTime(),
        rewards = redeemTemplate.rewards,
        sound = SoundState(
            sound = if (redeemTemplate.sound in Sound.entries.map { it.name }) Sound.valueOf(redeemTemplate.sound.uppercase())
            else null, volume = redeemTemplate.soundVolume, pitch = redeemTemplate.soundPitch
        ),
        messages = redeemTemplate.messages,
    )

    private fun upsertRedeemCode(redeemCode: RedeemCode) {
        try {
            val success = codeRepo.upsertCode(redeemCode)
            if (success) {
                placeHolder.code = redeemCode.code
                sendMessage(JMessage.Code.Generate.SUCCESS)
                generatedCodesList.add(redeemCode.code)
                JLogger(plugin).logGenerate(redeemCode.code + " - ${redeemCode.template}")
            } else sendMessage(JMessage.Code.Generate.FAILED)

        } catch (e: Exception) {
            sendMessage(JMessage.Code.Generate.FAILED)
            e.printStackTrace()
        }
    }

    private fun upsertRedeemCodes(redeemCodes: List<RedeemCode>) {
        val displayAmount = config.getConfigValue(JConfig.Code.DISPLAY_AMOUNT).toIntOrNull() ?: 40
        try {
            val success = codeRepo.upsertCodes(redeemCodes)
            if (success) {
                placeHolder.code = if (redeemCodes.size <= displayAmount) redeemCodes.joinToString(" ") { it.code }
                else redeemCodes.subList(0, displayAmount + 1).joinToString(" ") { it.code }.plus("...")
                redeemCodes.forEach { JLogger(plugin).logGenerate(it.code + " - ${it.template}") }
                sendMessage(JMessage.Code.Generate.SUCCESS)
                generatedCodesList.addAll(redeemCodes.map { it.code })
            } else {
                sendMessage(JMessage.Code.Generate.FAILED)
            }
        } catch (e: Exception) {
            sendMessage(JMessage.Code.Generate.FAILED)
            e.printStackTrace()
        }
    }

    private fun generateCode(length: Int): String? {
        val charset = ('A'..'Z') + ('0'..'9')
        repeat(1024) {
            // Max attempts
            val code = (1..length).map { charset.random() }.joinToString("")
            if (plugin.redeemCodeDB.get(code) == null) return code
        }
        return null // Failed to generate a unique code
    }
}
