package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.yml.JConfig
import me.justlime.redeemX.data.config.yml.JMessage
import me.justlime.redeemX.data.config.yml.JTemplate
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemCode
import org.bukkit.command.CommandSender

class GenerateSubCommand(private val plugin: RedeemX) : JSubCommand {
    private val service = plugin.service
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    var generatedCodesList = mutableListOf<String>()

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender, args)
        // Validate minimum arguments

        if (args.size < 3) {
            config.sendMsg("commands.gen.invalid-syntax", placeHolder)
            return false
        }

        val codeOrTemplate = args[1]
        var amount = 1
        if (args.size > 3 && args[1].equals("template", ignoreCase = true)) {
            amount = args[3].toIntOrNull() ?: 1
        }

        if (args.size > 2 && !args[1].equals("template", ignoreCase = true)) {
            placeHolder.code = args[2]
            amount = args[2].toIntOrNull() ?: 1
        }


        while (amount > 0) {
            when {
                codeOrTemplate.equals("template", ignoreCase = true) -> handleTemplateGeneration(args, placeHolder)
                codeOrTemplate.toIntOrNull() != null -> handleNumericGeneration(args, placeHolder)
                codeOrTemplate.matches(Regex("^[A-Z0-9]{3,10}$", RegexOption.IGNORE_CASE)) -> createUniqueCode(args[1], placeHolder)
                else -> config.sendMsg("commands.gen.invalid-code", placeHolder)
            }
            amount--
        }
        return true
    }

    private fun generateUniqueCode(length: Int, maxAttempts: Int = 1024, callback: (String?) -> Unit) {
        val charset = ('A'..'Z') + ('0'..'9')
        repeat(maxAttempts) {
            val code = (1..length).map { charset.random() }.joinToString("")
            if (plugin.redeemCodeDB.get(code) == null) {
                callback(code)
                return
            }
        }
        callback(null) // Failed to generate unique code within maxAttempts
    }

    private fun handleTemplateGeneration(args: MutableList<String>, placeHolder: CodePlaceHolder) {
        if (args.size < 3) {
            config.sendMsg("commands.gen.invalid-syntax", placeHolder)
            return
        }

        val template = args[2].lowercase()

        placeHolder.template = template

        // Fetch template data
        val tCommands = config.getTemplateValue(template, JTemplate.COMMANDS)
        val tDuration = config.getTemplateValue(template, JTemplate.DURATION)
        val tPermissionRequired = config.getTemplateValue(template, JTemplate.PERMISSION_REQUIRED).equals("true", ignoreCase = true)
        val tPermissionValue = config.getTemplateValue(template, JTemplate.PERMISSION_VALUE)

        val minLength = config.getConfigValue("code-minimum-digit").toIntOrNull() ?: 3
        val maxLength = config.getConfigValue("code-maximum-digit").toIntOrNull() ?: 10
        val codeGenerateDigit = config.getTemplateValue(template, JTemplate.CODE_GENERATE_DIGIT).toIntOrNull() ?: 5
        placeHolder.apply {
            this.template = template
            this.minLength = minLength.toString()
            this.maxLength = maxLength.toString()
            this.codeGenerateDigit = codeGenerateDigit.toString()
        }

        if (codeGenerateDigit !in minLength..maxLength) {
            config.sendMsg("commands.gen.invalid-range", placeHolder)
            return
        }

        generateUniqueCode(codeGenerateDigit) { uniqueCode ->
            if (uniqueCode == null) {
                config.sendMsg("commands.gen.length-error", placeHolder)
                return@generateUniqueCode
            }
            val redeemCode = RedeemCode(
                code = uniqueCode,
                commands = service.parseToMapId(service.parseToId(tCommands)),
                storedTime = service.currentTime,
                duration = tDuration,
                isEnabled = config.getTemplateValue(template, JTemplate.ENABLED).toBooleanStrictOrNull() ?: true,
                maxRedeems = config.getTemplateValue(template, JTemplate.MAX_REDEEMS).toIntOrNull() ?: 1,
                maxPlayers = config.getTemplateValue(template, JTemplate.MAX_PLAYERS).toIntOrNull() ?: 1,
                permission = if (tPermissionRequired) tPermissionValue.replace("{code}", uniqueCode) else "",
                pin = config.getTemplateValue(template, JTemplate.PIN).toIntOrNull() ?: -1,
                template = template,
                templateLocked = true,
                usage = mutableMapOf(),
                target = mutableListOf(),
                storedCooldown = service.currentTime, //TODO
                cooldown = "0s",
            )

            try {
                val success = codeRepo.upsertCode(redeemCode)
                if (!success) {
                    config.sendMsg("commands.gen.failed", placeHolder)
                    return@generateUniqueCode
                }
                config.sendMsg("commands.gen.success", placeHolder)
                generatedCodesList.add(uniqueCode)

            } catch (e: Exception) {
                config.sendMsg("commands.gen.error", placeHolder)
                e.printStackTrace()
            }

        }
    }

    private fun handleNumericGeneration(args: MutableList<String>, placeHolder: CodePlaceHolder) {
        val codeGenerateDigit = args[1].toIntOrNull() ?: return

        val minLength = config.getConfigValue("code-minimum-digit").toIntOrNull() ?: 3
        val maxLength = config.getConfigValue("code-maximum-digit").toIntOrNull() ?: 10
        placeHolder.codeGenerateDigit = codeGenerateDigit.toString()
        if (codeGenerateDigit !in minLength..maxLength) {
            config.sendMsg("commands.gen.invalid-range", placeHolder)
            return
        }

        generateUniqueCode(codeGenerateDigit) { uniqueCode ->
            if (uniqueCode == null) {
                config.sendMsg("commands.gen.length-error", placeHolder)
                return@generateUniqueCode
            }
            createUniqueCode(uniqueCode, placeHolder)
            generatedCodesList.add(uniqueCode)
        }
    }

    private fun createUniqueCode(uniqueCode: String, placeHolder: CodePlaceHolder) {
        placeHolder.code = uniqueCode
        if (codeRepo.getCode(uniqueCode) != null) {
            config.sendMsg(JMessage.Commands.Gen.CODE_ALREADY_EXIST, placeHolder)
            return
        }
        val configPermissionRequired = config.getConfigValue(JConfig.Default.PERMISSION.REQUIRED).equals("true",ignoreCase = true)
        val redeemCode = RedeemCode(
            code = uniqueCode,
            commands = service.parseToMapId(service.parseToId(config.getConfigValue(JConfig.Default.COMMANDS))),
            storedTime = service.currentTime,
            duration = config.getConfigValue(JConfig.Default.CODE_EXPIRED_DURATION),
            isEnabled = config.getConfigValue(JConfig.Default.ENABLED).toBooleanStrictOrNull() ?: true,
            maxRedeems = config.getConfigValue(JConfig.Default.MAX_REDEEMS).toIntOrNull() ?: 1,
            maxPlayers = config.getConfigValue(JConfig.Default.MAX_PLAYERS_CAN_REDEEM).toIntOrNull() ?: 1,
            permission = if (configPermissionRequired) config.getConfigValue(JConfig.Default.PERMISSION.VALUE) else "",
            pin = config.getConfigValue("default.pin").toIntOrNull() ?: 0,
            template = "",
            templateLocked = false,
            usage = mutableMapOf(),
            target = mutableListOf(),
            storedCooldown = service.currentTime,
            cooldown = config.getConfigValue("cooldown")
        )

        try {
            val success = codeRepo.upsertCode(redeemCode)
            if (!success) {
                config.sendMsg("commands.gen.failed", placeHolder)
                return
            }
            config.sendMsg("commands.gen.success", placeHolder)
            generatedCodesList.add(uniqueCode)

        } catch (e: Exception) {
            config.sendMsg("commands.gen.error", placeHolder)
            e.printStackTrace()
        }

    }

}
