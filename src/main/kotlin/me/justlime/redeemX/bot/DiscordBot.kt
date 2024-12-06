package me.justlime.redeemX.bot

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.ConfigManager
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
        jda?.updateCommands()?.addCommands(
            Commands.slash("generate", "Generate redeem codes")
                .addOption(OptionType.STRING, "template", "Template Name", false, true)
                .addOption(OptionType.INTEGER, "length", "Length of each code", false)
                .addOption(OptionType.INTEGER, "amount", "Number of codes to generate", false),
            Commands.slash("delete", "Delete codes")
                .addOption(OptionType.STRING, "code", "Code to delete", true,true),
            Commands.slash("modify","Modify a code")
                .addOption(OptionType.STRING,"code","Code to modify",true,true)
                .addOption(OptionType.STRING,"property","Property to modify",true,true)
                .addOption(OptionType.STRING,"value","New value",true,false)
        )?.queue()
    }

    fun stopBot() {
        jda?.shutdown()
    }
}