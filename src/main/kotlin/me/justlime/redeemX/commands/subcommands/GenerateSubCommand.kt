package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.CommandManager
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.enums.JSubCommand
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

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender, args)

        // Validate minimum arguments
        if (args.size < 2) {
            config.sendMsg("commands.gen.invalid-syntax", placeHolder)
            return false
        }

        val commandType = args[1].lowercase()
        val amount = if (commandType == "template") {
            args.getOrNull(3)?.toIntOrNull() ?: 1
        } else args.getOrNull(2)?.toIntOrNull() ?: 1

        when {
            amount == 1 -> {
                val redeemCode = when {
                    commandType == "template" -> handleTemplateGeneration(args, placeHolder)
                    commandType.matches(Regex("\\d+")) -> handleNumericGeneration(commandType.toInt(), placeHolder)
                    commandType.matches(Regex("^[A-Z0-9]{3,10}$", RegexOption.IGNORE_CASE)) -> handleCodeCreation(commandType, placeHolder)
                    else -> {
                        config.sendMsg("commands.gen.invalid-code", placeHolder)
                        return false
                    }
                }
                if (redeemCode != null) {
                    upsertRedeemCode(redeemCode, placeHolder)
                    
                    return true
                }
                config.sendMsg("commands.gen.error", placeHolder)
                return false
            }

            amount > 1 -> {
                val codes = mutableListOf<RedeemCode>()
                repeat(amount) {
                    val redeemCode = when {
                        commandType == "template" -> handleTemplateGeneration(args, placeHolder)
                        commandType.matches(Regex("\\d+")) -> handleNumericGeneration(args[1].toInt(), placeHolder)
                        commandType.matches(Regex("^[A-Z0-9]{3,10}$", RegexOption.IGNORE_CASE)) -> handleCodeCreation(args[1], placeHolder)
                        else -> {
                            config.sendMsg("commands.gen.invalid-code", placeHolder)
                            return@repeat
                        }
                    }
                    if (redeemCode != null) codes.add(redeemCode)
                    else return@repeat config.sendMsg("commands.gen.error", placeHolder)
                    if (commandType.matches(Regex("^[A-Z0-9]{3,10}$", RegexOption.IGNORE_CASE))) return@repeat

                }
                upsertRedeemCodes(codes, placeHolder)

            }

            else -> {
                config.sendMsg("commands.gen.invalid-amount", placeHolder)
            }
        }
        return true
    }

    private fun handleTemplateGeneration(args: MutableList<String>, placeHolder: CodePlaceHolder): RedeemCode? {
        val templateName = args.getOrNull(2)?.lowercase() ?: run {
            config.sendMsg("commands.gen.invalid-syntax", placeHolder)
            return null
        }
        val template = config.getTemplate(templateName) ?: return null
        val (minLength, maxLength) = loadCodeLengthRange(placeHolder)

        if (template.codeGenerateDigit !in minLength..maxLength) {
            config.sendMsg("commands.gen.invalid-range", placeHolder)
            return null
        }

        return generateCode(template.codeGenerateDigit) { uniqueCode ->
            if (uniqueCode == null) {
                config.sendMsg("commands.gen.length-error", placeHolder)
                return@generateCode null
            }
            val redeemCode = createRedeemCode(uniqueCode, template)
            return@generateCode redeemCode
        }
    }

    private fun handleNumericGeneration(codeLength: Int, placeHolder: CodePlaceHolder): RedeemCode? {
        val (minLength, maxLength) = loadCodeLengthRange(placeHolder)

        if (codeLength !in minLength..maxLength) {
            config.sendMsg("commands.gen.invalid-range", placeHolder)
            return null
        }

        return generateCode(codeLength) { uniqueCode ->
            if (uniqueCode == null) {
                config.sendMsg("commands.gen.length-error", placeHolder)
                return@generateCode null
            }
            handleCodeCreation(uniqueCode, placeHolder)
        }
    }

    private fun handleCodeCreation(code: String, placeHolder: CodePlaceHolder): RedeemCode? {
        placeHolder.code = code.uppercase()

        if (codeRepo.getCode(code.uppercase()) != null) {
            config.sendMsg(JMessage.Commands.Gen.CODE_ALREADY_EXIST, placeHolder)
            return null
        }

        val defaultConfig = loadDefaultConfig()
        val redeemCode = createRedeemCode(code.uppercase(), defaultConfig)
        return redeemCode
    }

    private fun loadDefaultConfig(): RedeemTemplate {
        return config.getTemplate() ?: RedeemTemplate(
            name = "default", commands = mutableMapOf(), duration = "0s", cooldown = "0s", maxRedeems = 1, maxPlayers = 1, templateLocked = false, permissionRequired = false, permissionValue = "", codeGenerateDigit = 5, message = mutableListOf()
        ).also {
            plugin.logger.warning("Default template configuration missing; fallback defaults applied.")
        }
    }

    private fun loadCodeLengthRange(placeHolder: CodePlaceHolder): Pair<Int, Int> {
        val minLength = config.getConfigValue("code-minimum-digit").toIntOrNull() ?: 3
        val maxLength = config.getConfigValue("code-maximum-digit").toIntOrNull() ?: 10
        placeHolder.minLength = minLength.toString()
        placeHolder.maxLength = maxLength.toString()
        return minLength to maxLength
    }

    private fun createRedeemCode(code: String, redeemTemplate: RedeemTemplate): RedeemCode {
        return RedeemCode(
            code = code,
            template = redeemTemplate.name,
            commands = redeemTemplate.commands,
            validFrom = service.getCurrentTime(),
            duration = redeemTemplate.duration,
            enabled = true,
            redemption = redeemTemplate.maxRedeems,
            limit = redeemTemplate.maxPlayers,
            permission = if (redeemTemplate.permissionRequired) redeemTemplate.permissionValue.replace("{code}", code) else "",
            pin = redeemTemplate.pin,
            locked = redeemTemplate.templateLocked,
            usedBy = mutableMapOf(),
            target = mutableListOf(),
            lastRedeemed = mutableMapOf(),
            cooldown = "0s"
        )
    }

    private fun upsertRedeemCode(redeemCode: RedeemCode, placeHolder: CodePlaceHolder) {
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

    private fun upsertRedeemCodes(redeemCodes: List<RedeemCode>, placeHolder: CodePlaceHolder) {
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

    private fun generateCode(length: Int, callback: (String?) -> RedeemCode?): RedeemCode? {
        val charset = ('A'..'Z') + ('0'..'9')
        repeat(1024) { // Max attempts
            val code = (1..length).map { charset.random() }.joinToString("")
            if (plugin.redeemCodeDB.get(code) == null) {
                return callback(code)
            }
        }
        return callback(null) // Failed to generate a unique code
    }

}
