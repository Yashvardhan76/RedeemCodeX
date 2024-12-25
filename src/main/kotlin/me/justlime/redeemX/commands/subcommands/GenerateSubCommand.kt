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
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.models.RedeemTemplate
import org.bukkit.command.CommandSender

class GenerateSubCommand(private val plugin: RedeemX) : JSubCommand {
    private val service = plugin.service
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    private val generatedCodesList = mutableListOf<String>()
    override var codeList: List<String> = generatedCodesList
    override val permission: String = JPermission.Admin.GEN
    lateinit var sender: CommandSender
    private var isDefaultLoaded = false
    private var usingDefault = false

    override fun execute(
        sender: CommandSender,
        args: MutableList<String>,
    ): Boolean {
        this.sender = sender
        val placeHolder = CodePlaceHolder(sender, args)
        // Validate minimum arguments
        if (!hasPermission(sender)) {
            config.sendMsg(JMessage.NO_PERMISSION, placeHolder)
            return true
        }
        if (args.size < 3) {
            config.sendMsg("commands.gen.invalid-syntax", placeHolder)
            return false
        }

        val type = args[1].lowercase()
        val digit = args[2].toIntOrNull()
        val amount = if(digit!= null) args.getOrNull(4)?.toIntOrNull() ?: 1 else 1

        if (args[1] == JTab.Type.Code.value && amount >= 1) {
            val codes = mutableListOf<RedeemCode>()
            for (index in 1..amount) {
                if (digit != null) {
                    handleNumericGeneration(digit, args.getOrNull(3) ?: "default", placeHolder)?.let { codes.add(it) }?: break
                } else {
                    handleCodeCreation(args[2], args.getOrNull(3) ?: "default", placeHolder)?.let { codes.add(it) } ?: return true
                }
            }
            if (usingDefault) config.sendMsg(JMessage.Commands.Gen.INVALID_TEMPLATE, placeHolder)
            if (isDefaultLoaded) config.sendMsg(JMessage.Commands.Gen.MISSING, placeHolder)
            if (codes.size == 1) {
                upsertRedeemCode(codes[0], placeHolder)
            } else upsertRedeemCodes(codes, placeHolder)

            CommandManager(plugin).tabCompleterList.fetched()

            // Reset
            usingDefault = false
            isDefaultLoaded = false

            return true
        }
        if (args[1] == JTab.Type.Code.value && amount < 1) config.sendMsg(JMessage.Commands.Gen.INVALID_AMOUNT, placeHolder)
        if (type == JTab.Type.Template.value) {
            generateTemplate(args[2], placeHolder)
            CommandManager(plugin).tabCompleterList.fetched()
            if (isDefaultLoaded) config.sendMsg(JMessage.Commands.Gen.MISSING, placeHolder)
            // Reset
            isDefaultLoaded = false

            return true
        }
        config.sendMsg(JMessage.Commands.Help.UNKNOWN_COMMAND, placeHolder)
        return false
    }

    private fun generateTemplate(
        templateName: String,
        placeHolder: CodePlaceHolder,
    ) {
        placeHolder.template = templateName
        if (config.getTemplate(templateName) != null) return config.sendMsg(JMessage.Commands.GenTemplate.ALREADY_EXIST, placeHolder)
        val template = config.getTemplate("default") ?: loadDefaultConfig()
        template.name = templateName
        template.locked = true
        template.commands = mutableMapOf()
        template.message = mutableListOf()
        config.createTemplate(template)
        config.sendMsg(JMessage.Commands.GenTemplate.SUCCESS, placeHolder)
    }

    private fun handleNumericGeneration(
        codeLength: Int,
        templateName: String,
        placeHolder: CodePlaceHolder,
    ): RedeemCode? {
        placeHolder.template = templateName
        val (minLength, maxLength) = loadCodeLengthRange(placeHolder)
        if (codeLength !in minLength..maxLength) {
            config.sendMsg("commands.gen.invalid-range", placeHolder)
            return null
        }
        val uniqueCode = generateCode(codeLength)
        if (uniqueCode == null) {
            config.sendMsg("commands.gen.length-error", placeHolder)
            return null
        }
        placeHolder.code = uniqueCode
        val template = config.getTemplate(templateName) ?: config.getTemplate("default") ?: loadDefaultConfig()
        if (templateName != "default" && template.name == "default") usingDefault = true
        return createRedeemCode(uniqueCode, template)
    }

    private fun handleCodeCreation(
        code: String,
        templateName: String,
        placeHolder: CodePlaceHolder,
    ): RedeemCode? {
        placeHolder.template = templateName
        placeHolder.code = code.uppercase()

        if (codeRepo.getCode(code.uppercase()) != null) {
            config.sendMsg(JMessage.Commands.Gen.CODE_ALREADY_EXIST, placeHolder)
            return null
        }

        val template = config.getTemplate(templateName) ?: config.getTemplate("default") ?: loadDefaultConfig()
        if (templateName != "default" && template.name == "default") usingDefault = true
        val redeemCode = createRedeemCode(code.uppercase(), template)
        return redeemCode
    }

    private fun loadDefaultConfig(): RedeemTemplate {
        isDefaultLoaded = true
        return config.getTemplate() ?: RedeemTemplate(
            name = "default",
            commands = mutableMapOf(),
            duration = "0s",
            cooldown = "0s",
            redemption = 1,
            playerLimit = 1,
            locked = false,
            permissionRequired = false,
            permissionValue = "",
            message = mutableListOf(),
        )
    }

    private fun loadCodeLengthRange(placeHolder: CodePlaceHolder): Pair<Int, Int> {
        val minLength = config.getConfigValue("code-minimum-digit").toIntOrNull() ?: 3
        val maxLength = config.getConfigValue("code-maximum-digit").toIntOrNull() ?: 10
        placeHolder.minLength = minLength.toString()
        placeHolder.maxLength = maxLength.toString()
        return minLength to maxLength
    }

    private fun createRedeemCode(
        code: String,
        redeemTemplate: RedeemTemplate,
    ): RedeemCode = RedeemCode(
        code = code.uppercase(),
        template = redeemTemplate.name,
        commands = redeemTemplate.commands,
        validFrom = service.getCurrentTime(),
        duration = redeemTemplate.duration,
        enabled = true,
        redemption = redeemTemplate.redemption,
        playerLimit = redeemTemplate.playerLimit,
        permission = if (redeemTemplate.permissionRequired) redeemTemplate.permissionValue.replace("{code}", code) else "",
        pin = redeemTemplate.pin,
        locked = redeemTemplate.locked,
        usedBy = mutableMapOf(),
        target = mutableListOf(),
        lastRedeemed = mutableMapOf(),
        cooldown = "0s",
        modified = service.getCurrentTime(),
    )

    private fun upsertRedeemCode(
        redeemCode: RedeemCode,
        placeHolder: CodePlaceHolder,
    ) {
        try {
            val success = codeRepo.upsertCode(redeemCode)
            if (success) {
                placeHolder.code = redeemCode.code
                config.sendMsg("commands.gen.success", placeHolder)
                CommandManager(plugin).tabCompleterList.fetched()
                generatedCodesList.add(redeemCode.code)
            } else {
                config.sendMsg("commands.gen.failed", placeHolder)
            }
        } catch (e: Exception) {
            config.sendMsg("commands.gen.error", placeHolder)
            e.printStackTrace()
        }
    }

    private fun upsertRedeemCodes(
        redeemCodes: List<RedeemCode>,
        placeHolder: CodePlaceHolder,
    ) {
        try {
            val success = codeRepo.upsertCodes(redeemCodes)
            if (success) {
                placeHolder.code = redeemCodes.joinToString(" ") { it.code }
                config.sendMsg("commands.gen.success", placeHolder)
                CommandManager(plugin).tabCompleterList.fetched()
                generatedCodesList.addAll(redeemCodes.map { it.code })
            } else {
                config.sendMsg("commands.gen.failed", placeHolder)
            }
        } catch (e: Exception) {
            config.sendMsg("commands.gen.error", placeHolder)
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
