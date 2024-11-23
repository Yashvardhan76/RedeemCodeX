package me.justlime.redeemX.bot

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.GenerateSubCommand
import me.justlime.redeemX.config.ConfigManager
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Bukkit

class DiscordCommandListener(val plugin: RedeemX) : ListenerAdapter() {
    private val generateSubCommand = GenerateSubCommand(plugin)
    val config: ConfigManager = ConfigManager(plugin)
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == "generate") {

//            event.reply("Generating $amount codes with length $length").queue()

            // Logic to generate codes and return them to the user
            val codes = handleGenerateCommand(event)
//            event.hook.sendMessage("Generated codes: $codes").queue()
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val cachedCodes = plugin.redeemCodeDB
        val templateCodes: List<String> = config.getTemplateNames()


        if (event.name == "generate" && event.focusedOption.name == "template") {
            event.replyChoiceStrings(templateCodes.filter {
                it.startsWith(event.focusedOption.value)
            }).queue()
        }
    }
    private fun handleGenerateCommand(event: SlashCommandInteractionEvent) {
        val template = event.getOption("template")
        val length = event.getOption("length")?.asInt ?: config.getString("code-minimum-digit")?.toIntOrNull() ?: 5
        val amount = event.getOption("amount")?.asInt ?: 1

        // Mimic a CommandSender (Console for Discord commands)
        val sender = Bukkit.getConsoleSender()

        // Arguments to pass to GenerateSubCommand
        var args = arrayOf("generate", length.toString(), amount.toString())
        if (template!=null) args = arrayOf("generate", "template", template.asString)

        // Schedule task to execute on Bukkit's main thread
        Bukkit.getScheduler().runTask(plugin, Runnable {
            try {


                event.reply("Generating codes...").setEphemeral(false).queue { _ ->
                    generateSubCommand.execute(sender, args)

                    val generatedCodes = generateSubCommand.generatedSubCommand
                    if (generatedCodes.isEmpty()) {
                        event.reply("No codes were generated. Please check your input.").setEphemeral(true).queue()
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

}
