package me.justlime.redeemX.bot

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.GenerateSubCommand
import me.justlime.redeemX.commands.subcommands.ModifySubCommand
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.config.Files
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import org.bukkit.Bukkit

class DiscordCommandListener(val plugin: RedeemX) : ListenerAdapter() {
    private val generateSubCommand = GenerateSubCommand(plugin)
    private val sender = Bukkit.getConsoleSender()
    private val db = plugin.redeemCodeDB
    val config: ConfigManager = ConfigManager(plugin)
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == "generate") return handleGenerateCommand(event)
        if (event.name == "delete") {
            val code = event.getOption("code").toString()
            val cachedCodes = db.getFetchCodes
            if (code.isEmpty() || code.isBlank() || cachedCodes.contains(code)) return


            Bukkit.getScheduler().runTask(plugin, Runnable {
                try {
                    val delMessage = config.getString("commands.delete.success", Files.MESSAGES)?.replace("{code}", code) ?: ""
                    event.reply("Deleting code...").setEphemeral(false).queue()
                    val success = db.deleteByCode(code)

                    if (success) config.getString("commands.delete.success", Files.MESSAGES)?.let {
                        event.reply(it).setEphemeral(false).queue() { _ ->
                            db.deleteByCode(code)
                            event.hook.editOriginal(delMessage).queue()
                        }
                    }
                    else config.getString("commands.delete.failed", Files.MESSAGES)?.let { event.reply(it) }

                } catch (_: Exception) {

                }
            })

        }
        if (event.name == "modify") return handleModifyCommand(event)
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val focusedOption = event.focusedOption

        val choices = when (event.name) {
            "generate" -> {
                if (focusedOption.name == "template") {
                    config.getTemplateNames()
                        .filter { it.isNotBlank() && it.startsWith(focusedOption.value, ignoreCase = true) }
                        .map { Command.Choice(it, it) }
                } else {
                    emptyList() // Handle cases where the focused option is not "template"
                }
            }
            "delete", "modify" -> {
                if (focusedOption.name == "code") {
                    plugin.redeemCodeDB.getFetchCodes
                        .filter { it.isNotBlank() && it.startsWith(focusedOption.value, ignoreCase = true) }
                        .map { Command.Choice(it, it) }

                } else {
                    emptyList()
                }
            }
            else -> emptyList() // Handle other command names or unexpected situations
        }.take(25)  // Ensure we never exceed 25 choices


        event.replyChoices(choices).queue()

    }

    private fun handleGenerateCommand(event: SlashCommandInteractionEvent) {
        val template = event.getOption("template")
        val length = event.getOption("length")?.asInt ?: config.getString("code-minimum-digit")?.toIntOrNull() ?: 5
        val amount = event.getOption("amount")?.asInt ?: 1

        // Arguments to pass to GenerateSubCommand
        var args = arrayOf("generate", length.toString(), amount.toString())
        if (template != null) args = arrayOf("generate", "template", template.asString)

        // Schedule task to execute on Bukkit's main thread
        Bukkit.getScheduler().runTask(plugin, Runnable {
            try {

                event.reply("Generating codes...").setEphemeral(false).queue { _ ->
                    generateSubCommand.execute(sender, args)

                    val generatedCodes = generateSubCommand.generatedSubCommand
                    if (generatedCodes.isEmpty()) {
                        event.hook.editOriginal("No codes were generated. Please check your input.").queue()
                        return@queue
                    }

                    val response = buildString {
                        append("Successfully generated ${generatedCodes.size} code(s):\n")
                        generatedCodes.forEach { append("`$it`\n") }
                    }

                    event.hook.editOriginal(response).queue()
                    generateSubCommand.generatedSubCommand.clear()
                }
            } catch (e: Exception) {
                event.reply("Failed to generate codes due to an error: ${e.message}").setEphemeral(true).queue()
                e.printStackTrace()
            }
        })
    }

    private fun handleModifyCommand(event: SlashCommandInteractionEvent) {
        val code = event.getOption("code")?.asString
        val property = event.getOption("property")?.asString?.lowercase()
        val value = event.getOption("value")?.asString

        if (code.isNullOrEmpty() || property.isNullOrEmpty() || value.isNullOrEmpty()) {
            event.reply("Invalid input. Please provide all required parameters.").setEphemeral(true).queue()
            return
        }

        Bukkit.getScheduler().runTask(plugin, Runnable {
            try {
                val args = mutableListOf("modify", code, property, value)
                val state = plugin.stateManager.createState(sender, code)
                state.args = args
                val success = ModifySubCommand(plugin).execute(state)

                if (success) {
                    event.reply("Code '${code}' modified successfully.").setEphemeral(false).queue()
                } else {
                    event.reply("Failed to modify code '${code}'. Please check the logs.").setEphemeral(true).queue()
                }
            } catch (e: Exception) {
                event.reply("An unexpected error occurred: ${e.message}").setEphemeral(true).queue()
                e.printStackTrace()
            }
        })
    }

}
