package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.config.Files
import me.justlime.redeemX.state.RedeemCodeState
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.command.CommandSender

class GenerateSubCommand(private val plugin: RedeemX) {
    private val config = ConfigManager(plugin)
    private val stateManager = plugin.stateManager
    private val service = RedeemCodeService(plugin)
    var generatedSubCommand = mutableListOf<String>()


    fun execute(sender: CommandSender, args: Array<out String>) {
        val state = stateManager.createState(sender).apply { this.args = args.toMutableList() }

        // Validate minimum arguments
        if (state.args.size < 2) {
            config.sendMessage("commands.gen.invalid-syntax", state)
            return
        }

        state.inputTemplate = args[1]
        val cached = state.inputTemplate
        var amount = 1
        if (args.size > 2 && !args[1].equals("template", ignoreCase = true)) {
            amount = args[2].toIntOrNull() ?: 1
        }

        if (args.size > 3 && args[1].equals("template", ignoreCase = true)) {
            amount = args[3].toIntOrNull() ?: 1

        }
        while (amount > 0) {
            when {
                state.inputTemplate.equals("template", ignoreCase = true) -> handleTemplateGeneration(state)
                state.inputTemplate.toIntOrNull() != null -> handleNumericGeneration(state)
                state.inputTemplate.matches(Regex("^[A-Z0-9]{3,10}$", RegexOption.IGNORE_CASE)) -> handleCustomCode(state)
                else -> config.sendMessage("commands.gen.invalid-code", state)
            }
            state.inputTemplate = cached
            amount--

        }

        stateManager.clearState(state.sender)

    }

    private fun handleTemplateGeneration(state: RedeemCodeState) {
        val templateName = state.args.getOrNull(2)?.lowercase() ?: run {
            config.sendMessage("commands.gen.invalid-syntax", state)
            return
        }

        state.templateName = templateName
        val templateConfigPath = state.templateName

        // Fetch template data
        val tCommands =
            config.getString("$templateConfigPath.commands", Files.TEMPLATE)?.removePrefix("[")?.removeSuffix("]")
        val tDuration = service.adjustDuration(
            "0s",
            config.getString("$templateConfigPath.code-expired-duration", Files.TEMPLATE).orEmpty(),
            isAdding = true
        )
        val tPermissionRequired = config.getString("$templateConfigPath.permission.required", Files.TEMPLATE)
            ?.equals("true", ignoreCase = true) ?: false

        state.minLength = config.getString("code-minimum-digit")?.toIntOrNull() ?: 3
        state.maxLength = config.getString("code-maximum-digit")?.toIntOrNull() ?: 10
        state.inputTemplate = config.getString("$templateConfigPath.code-generate-digit", Files.TEMPLATE) ?: "5"

        val codeLength = state.inputTemplate.toIntOrNull() ?: run {
            config.sendMessage("commands.gen.invalid-range", state)
            return
        }

        if (codeLength !in state.minLength..state.maxLength) {
            config.sendMessage("commands.gen.invalid-range", state)
            return
        }

        generateUniqueCode(codeLength) { uniqueCode ->
            if (uniqueCode == null) {
                config.sendMessage("commands.gen.length-error", state)
                return@generateUniqueCode
            }
            state.inputTemplate = uniqueCode

            state.apply {
                this.inputCode = this.inputTemplate
                this.code = this.inputTemplate
                this.templateName = templateName
                this.commands = service.parseToMapId(service.parseToId(tCommands))
                this.duration = "${tDuration}s"
                this.storedTime = if (tDuration > 1) service.currentTime else null
                this.isEnabled =
                    config.getString("$templateConfigPath.enabled", Files.TEMPLATE)?.equals("true", ignoreCase = true)
                        ?: false
                this.maxRedeems =
                    config.getString("$templateConfigPath.max_redeems", Files.TEMPLATE)?.toIntOrNull() ?: 1
                this.maxPlayers = config.getString("$templateConfigPath.max_player", Files.TEMPLATE)?.toIntOrNull() ?: 1
                this.permission = if (tPermissionRequired) {
                    config.getString("$templateConfigPath.permission.value", Files.TEMPLATE)
                        ?.replace("{code}", this.inputTemplate)
                } else null
                this.pin = config.getString("$templateConfigPath.pin", Files.TEMPLATE)?.toIntOrNull() ?: -1
            }

            createRedeemCode(state)
        }
    }

    private fun handleNumericGeneration(state: RedeemCodeState) {
        val codeLength = state.inputTemplate.toIntOrNull() ?: return

        val minLength = config.getString("code-minimum-digit")?.toIntOrNull() ?: 3
        val maxLength = config.getString("code-maximum-digit")?.toIntOrNull() ?: 10

        if (codeLength !in minLength..maxLength) {
            config.sendMessage("commands.gen.invalid-range", state)
            return
        }

        generateUniqueCode(codeLength) { uniqueCode ->
            if (uniqueCode == null) {
                config.sendMessage("commands.gen.length-error", state)
                return@generateUniqueCode
            }
            state.inputTemplate = uniqueCode
            createRedeemCode(state)
        }
    }

    private fun handleCustomCode(state: RedeemCodeState) {
        if (stateManager.fetchState(state.sender, state.inputTemplate)) {
            config.sendMessage("commands.gen.code-already-exist", state)
            return
        }
        createRedeemCode(state)
    }

    private fun createRedeemCode(state: RedeemCodeState) {
        if (!state.args[1].equals("template", ignoreCase = true)) {
            // Retrieve defaults from the configuration
            val commands = config.getString("default.commands")?.removePrefix("[")?.removeSuffix("]")
            val defaultDuration = config.getString("default.code-expired-duration") ?: "0s"
            val defaultEnabled = config.getString("default.enabled")?.toBooleanStrictOrNull() ?: true
            val defaultMaxRedeems = config.getString("default.max_redeems")?.toIntOrNull() ?: 1
            val defaultMaxPlayers = config.getString("default.max_player")?.toIntOrNull() ?: 1
            val defaultPin = config.getString("default.pin")?.toIntOrNull() ?: -1
            val permissionRequired = config.getString("default.permission.required")?.toBooleanStrictOrNull() ?: false
            val defaultPermissionValue = config.getString("default.permission.value") ?: "redeemx.use.{code}"

            // Populate state with default values

            state.apply {
                this.inputCode = this.inputTemplate
                this.commands = service.parseToMapId(service.parseToId(commands))
                this.duration = "${service.adjustDuration("0s", defaultDuration, true)}s"
                this.storedTime = if (defaultDuration != "0s") service.currentTime else null
                this.isEnabled = defaultEnabled
                this.maxRedeems = defaultMaxRedeems
                this.maxPlayers = defaultMaxPlayers
                this.permission = if (permissionRequired) {
                    defaultPermissionValue.replace("{code}", inputTemplate)
                } else null
                this.pin = defaultPin
            }
        }

        try {
            // Insert the redeem code into the database
            state.code = state.inputTemplate
            state.inputCode = state.inputTemplate
            val success = stateManager.updateDb(state.sender)
            if (!success) {
                config.sendMessage("commands.gen.failed", state)
                return
            }
            config.sendMessage("commands.gen.success", state)
            generatedSubCommand.add(state.inputTemplate)

        } catch (e: Exception) {
            config.sendMessage("commands.gen.error", state)
            e.printStackTrace()
        }
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
}
