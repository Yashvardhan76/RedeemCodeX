package me.justlime.redeemX.bot

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX
import me.justlime.redeemX.config.ConfigManager

\.config.ConfigManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

class DiscordBot(val plugin: RedeemX) {

    private val configManager = ConfigManager(plugin)
    private val botToken = configManager.getString("bot.token")
    private var jda: JDA? = null

    fun startBot() {
        jda = JDABuilder.createDefault(botToken).build()
        jda?.addEventListener(DiscordCommandListener(plugin))

        // Delete all existing commands

        // Re-register the updated commands
        jda?.updateCommands()?.addCommands(
            Commands.slash("generate", "Generate redeem codes")
                .addOption(OptionType.STRING, "template", "Template Name", false, true)
                .addOption(OptionType.INTEGER, "length", "Length of each code", false)
                .addOption(OptionType.INTEGER, "amount", "Number of codes to generate", false)
        )?.queue()
    }

    fun stopBot() {
        jda?.shutdown()
    }
}
