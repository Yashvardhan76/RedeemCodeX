package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JConfig
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JPermission
import me.justlime.redeemX.enums.JSubCommand
import me.justlime.redeemX.enums.JTab
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.models.RedeemTemplate
import me.justlime.redeemX.utilities.JService
import org.bukkit.command.CommandSender

class GenerateSubCommand(private val plugin: RedeemX) : JSubCommand {
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    private val generatedCodesList = mutableListOf<String>()
    override var jList: List<String> = generatedCodesList
    override val permission: String = JPermission.Admin.GEN
    lateinit var sender: CommandSender
    private var isDefaultLoaded = false
    private var usingDefault = false

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        this.sender = sender
        val placeHolder = CodePlaceHolder(sender, args)
        // Validate minimum arguments
        if (!hasPermission(sender)) {
            config.sendMsg(JMessage.Command.NO_PERMISSION, placeHolder)
            return true
        }
        if (args.size < 3) {
            config.sendMsg(JMessage.Command.UNKNOWN_COMMAND, placeHolder)
            return false
        }

        val type = args[1].lowercase()
        val digit = args[2].toIntOrNull()
        val amount = if (digit != null) args.getOrNull(4)?.toIntOrNull() ?: 1 else 1

        if (args[1] == JTab.Type.CODE && amount >= 1) {
            val codes = mutableListOf<RedeemCode>()
            for (index in 1..amount) {
                if (digit != null) {
                    handleNumericGeneration(digit, args.getOrNull(3) ?: "DEFAULT", placeHolder)?.let { codes.add(it) } ?: break
                } else {
                    handleCodeCreation(args[2], args.getOrNull(3) ?: "DEFAULT", placeHolder)?.let { codes.add(it) } ?: return true
                }
            }
            if (isDefaultLoaded) config.sendMsg(JMessage.Code.Generate.MISSING, placeHolder)
            if (codes.size == 1) {
                upsertRedeemCode(codes[0], placeHolder)
            } else upsertRedeemCodes(codes, placeHolder)

//            CommandManager(plugin).tabCompleterList.fetched()

            // Reset
            usingDefault = false
            isDefaultLoaded = false

            return true
        }
        if (args[1] == JTab.Type.CODE && amount < 1) config.sendMsg(JMessage.Code.Generate.INVALID_AMOUNT, placeHolder)
        if (type == JTab.Type.TEMPLATE) {
            generateTemplate(args[2].uppercase(), placeHolder)
//            CommandManager(plugin).tabCompleterList.fetched()
            if (isDefaultLoaded) config.sendMsg(JMessage.Code.Generate.MISSING, placeHolder)
            // Reset
            isDefaultLoaded = false

            return true
        }
        config.sendMsg(JMessage.Command.UNKNOWN_COMMAND, placeHolder)
        return false
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String>? {
        val cachedTemplate = config.getAllTemplates().map { it.name }
        val completions = mutableListOf<String>()

        if (!hasPermission(sender)) return mutableListOf()
        when (args.size) {
            2 -> { completions.addAll(mutableListOf(JTab.Type.CODE, JTab.Type.TEMPLATE)) }

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
        return completions.filter {
            it.contains(args.lastOrNull() ?: "", ignoreCase = true)
        }.sortedBy { it.lowercase() }.toMutableList()
    }

    private fun generateTemplate(templateName: String, placeHolder: CodePlaceHolder) {
        placeHolder.template = templateName
        if (config.getTemplate(templateName) != null) return config.sendMsg(JMessage.Template.Generate.ALREADY_EXIST, placeHolder)
        val template = config.loadDefaultTemplateValues(templateName)
        template.defaultSync = true
        config.createTemplate(template)
        config.sendMsg(JMessage.Template.Generate.SUCCESS, placeHolder)
    }

    private fun handleNumericGeneration(codeLength: Int, templateName: String, placeHolder: CodePlaceHolder): RedeemCode? {
        placeHolder.template = templateName
        val (minLength, maxLength) = config.getCodeLengthRange(placeHolder)
        if (codeLength !in minLength..maxLength) {
            config.sendMsg(JMessage.Code.Generate.INVALID_RANGE, placeHolder)
            return null
        }
        val uniqueCode = generateCode(codeLength)
        if (uniqueCode == null) {
            config.sendMsg(JMessage.Code.Generate.INVALID_LENGTH, placeHolder)
            return null
        }
        placeHolder.code = uniqueCode
        val template = config.getTemplate(templateName) ?: return null
        return createRedeemCode(uniqueCode, template)
    }

    private fun handleCodeCreation(code: String, templateName: String, placeHolder: CodePlaceHolder): RedeemCode? {
        placeHolder.template = templateName
        placeHolder.code = code.uppercase()

        if (codeRepo.getCode(code.uppercase()) != null) {
            config.sendMsg(JMessage.Code.Generate.ALREADY_EXIST, placeHolder)
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
        rewards = mutableListOf()
    )

    private fun upsertRedeemCode(redeemCode: RedeemCode, placeHolder: CodePlaceHolder) {
        try {
            val success = codeRepo.upsertCode(redeemCode)
            if (success) {
                placeHolder.code = redeemCode.code
                config.sendMsg(JMessage.Code.Generate.SUCCESS, placeHolder)
//                CommandManager(plugin).tabCompleterList.fetched()
                generatedCodesList.add(redeemCode.code)
            } else {
                config.sendMsg(JMessage.Code.Generate.FAILED, placeHolder)
            }
        } catch (e: Exception) {
            config.sendMsg(JMessage.Code.Generate.FAILED, placeHolder)
            e.printStackTrace()
        }
    }

    private fun upsertRedeemCodes(redeemCodes: List<RedeemCode>, placeHolder: CodePlaceHolder) {
        val displayAmount = config.getConfigValue(JConfig.Code.DISPLAY_AMOUNT).toIntOrNull() ?: 40
        try {
            val success = codeRepo.upsertCodes(redeemCodes)
            if (success) {
                placeHolder.code = if (redeemCodes.size <= displayAmount) redeemCodes.joinToString(" ") { it.code }
                else redeemCodes.subList(0, displayAmount + 1).joinToString(" ") { it.code }.plus("...")
                config.sendMsg(JMessage.Code.Generate.SUCCESS, placeHolder)
//                CommandManager(plugin).tabCompleterList.fetched()
                generatedCodesList.addAll(redeemCodes.map { it.code })
            } else {
                config.sendMsg(JMessage.Code.Generate.FAILED, placeHolder)
            }
        } catch (e: Exception) {
            config.sendMsg(JMessage.Code.Generate.FAILED, placeHolder)
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
