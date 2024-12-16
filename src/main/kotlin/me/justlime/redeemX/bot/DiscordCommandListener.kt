package me.justlime.redeemX.bot

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.GenerateSubCommand
import me.justlime.redeemX.commands.subcommands.ModifySubCommand
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.models.CodePlaceHolder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import org.bukkit.Bukkit

class DiscordCommandListener(val plugin: RedeemX) : ListenerAdapter() {
    private val generateSubCommand = GenerateSubCommand(plugin)
    private val sender = Bukkit.getConsoleSender()
    val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val placeHolder: CodePlaceHolder
        if (event.name == "generate") return handleGenerateCommand(event)
        if (event.name == "delete") {
            val code = event.getOption("code").toString()
            val cachedCodes = codeRepo.getCachedCode()
            if (code.isEmpty() || code.isBlank() || cachedCodes.contains(code)) return

            placeHolder = CodePlaceHolder.fetchByDB(plugin,code,sender)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                try {
                    val delMessage = config.getMessage("commands.delete.success",placeHolder)
                    event.reply("Deleting code...").setEphemeral(false).queue()
                    val success = codeRepo.deleteCode(code)

                    if (success) config.getMessage("commands.delete.success",placeHolder).let {
                        event.reply(it).setEphemeral(false).queue { _ ->
                            event.hook.editOriginal(delMessage).queue()
                        }
                    }
                    else config.getMessage("commands.delete.failed",placeHolder).let { event.reply(it) }

                } catch (_: Exception) {
                    event.reply(config.getMessage("commands.delete.failed",placeHolder)).setEphemeral(true).queue()
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
                    config.getAllTemplates()
                        .asSequence() // Use sequence for efficiency with larger data sets.
                        .filter { it.name.isNotBlank() && it.name.startsWith(focusedOption.value, ignoreCase = true) }
                        .map { Command.Choice(it.name, it.name) }
                        .toList() // Convert the sequence back to a list, if necessary.
                } else {
                    emptyList() // Handle cases where the focused option is not "template"
                }
            }

            "delete" -> {
                if (focusedOption.name == "code") {
                    codeRepo.getCachedCode().filter { it.isNotBlank() && it.startsWith(focusedOption.value, ignoreCase = true) }.map { Command.Choice(it, it) }

                } else {
                    emptyList()
                }
            }

            "modify" -> {
                when (focusedOption.name) {
                    "code" -> {
                        codeRepo.getCachedCode().filter { it.isNotBlank() && it.startsWith(focusedOption.value, ignoreCase = true) }.map { Command.Choice(it, it) }
                    }

                    "property" -> {
                        listOf(
                            "enabled", "max_redeems", "max_player", "setDuration", "unsetDuration", "addDuration", "removeDuration", "permission", "pin", "addTarget", "removeTarget", "addCommand", "removeCommand", "setCommand", "list"

                        ).filter { it.isNotBlank() && it.startsWith(focusedOption.value, ignoreCase = true) }.map { Command.Choice(it, it) }
                    }

                    else -> {
                        emptyList()
                    }
                }
            }

            else -> emptyList()
        }.take(25)  // Ensure we never exceed 25 choices


        event.replyChoices(choices).queue()

    }

    private fun handleGenerateCommand(event: SlashCommandInteractionEvent) {
        val template = event.getOption("template")
        val length = event.getOption("length")?.asInt ?: config.getConfigValue("code-minimum-digit").toIntOrNull() ?: 5
        val amount = event.getOption("amount")?.asInt ?: 1

        // Arguments to pass to GenerateSubCommand
        var args = mutableListOf("generate", length.toString(), amount.toString())
        if (template != null) args = mutableListOf("generate", "template", template.asString)

        // Schedule task to execute on Bukkit's main thread
        Bukkit.getScheduler().runTask(plugin, Runnable {
            try {

                event.reply("Generating codes...").setEphemeral(false).queue { _ ->
                    generateSubCommand.execute(sender, args)

                    val generatedCodes = generateSubCommand.generatedCodesList
                    if (generatedCodes.isEmpty()) {
                        event.hook.editOriginal("No codes were generated. Please check your input.").queue()
                        return@queue
                    }

                    val response = buildString {
                        append("Successfully generated ${generatedCodes.size} code(s):\n")
                        generatedCodes.forEach { append("`$it`\n") }
                    }

                    event.hook.editOriginal(response).queue()
                    generateSubCommand.generatedCodesList.clear()
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

        val invalidInputMsg = "Invalid input. Please provide all required parameters."

        if (code.isNullOrEmpty() || property.isNullOrEmpty() || value.isNullOrEmpty()) {
            event.reply(invalidInputMsg).setEphemeral(true).queue()
            return
        }


        Bukkit.getScheduler().runTask(plugin, Runnable {
            try {
                val args = mutableListOf("modify", code)
                when {
                    property.equals("addTarget",ignoreCase = true)  -> {
                        val target = event.getOption("value")?.asString
                        if (target.isNullOrEmpty()) return@Runnable event.reply(invalidInputMsg).setEphemeral(true).queue()
                        args.add("target")
                        args.add("add")
                        args.add(value)
                    }

                    property.equals("removeTarget",ignoreCase = true) -> {
                        val target = event.getOption("value")?.asString
                        if (target.isNullOrEmpty()) return@Runnable event.reply(invalidInputMsg).setEphemeral(true).queue()
                        args.add("target")
                        args.add("remove")
                        args.add(value)

                    }

                    property.equals("removeAllTarget",ignoreCase = true) -> {
                        args.add("target")
                        args.add("remove_all")
                    }

                    property.equals("addCommand",ignoreCase = true) -> {
                        val command = event.getOption("value")?.asString
                        if (command.isNullOrEmpty()) return@Runnable event.reply(invalidInputMsg).setEphemeral(true).queue()
                        args.add("command")
                        args.add("add")
                        args.add(command)
                    }

                    property.equals("removeCommand",ignoreCase = true) -> {
                        val command = event.getOption("value")?.asString
                        if (command.isNullOrEmpty()) return@Runnable event.reply("Invalid input. Please provide all required parameters.").setEphemeral(true).queue()
                        args.add("command")
                        args.add("remove")
                        args.add(command)
                    }

                    property.equals("setCommand",ignoreCase = true) -> {
                        val command = event.getOption("value")?.asString
                        if (command.isNullOrEmpty()) return@Runnable event.reply("Invalid input. Please provide all required parameters.").setEphemeral(true).queue()
                        args.add("command")
                        args.add("set")
                        args.add(command)
                    }

                    property.equals("setDuration",ignoreCase = true) -> {
                        args.add("duration")
                        args.add("set")
                        args.add(value)
                    }

                    property.equals("unsetDuration",ignoreCase = true) -> {
                        args.add("duration")
                        args.add("set")
                        args.add("0s")
                    }

                    property.equals("addDuration",ignoreCase = true) -> {
                        val duration = event.getOption("value")?.asString
                        if (duration.isNullOrEmpty()) return@Runnable event.reply(invalidInputMsg).setEphemeral(true).queue()
                        args.add("duration")
                        args.add("add")
                        args.add(duration)
                    }

                    property.equals("removeDuration",ignoreCase = true) -> {
                        val duration = event.getOption("value")?.asString
                        if (duration.isNullOrEmpty()) return@Runnable event.reply(invalidInputMsg).setEphemeral(true).queue()
                        args.add("duration")
                        args.add("remove")
                    }

                    else -> {
                        args.add(property)
                        args.add(value)
                    }
                }
                //send msg args
                plugin.logger.info("Discord Modify Command Args: $args")

                val success = ModifySubCommand(plugin).execute(sender, args)

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
