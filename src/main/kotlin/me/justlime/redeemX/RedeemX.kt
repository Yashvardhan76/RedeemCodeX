package me.justlime.redeemX

import me.justlime.redeemX.bot.DiscordBot
import me.justlime.redeemX.commands.CommandManager
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.dao.RedeemCodeDaoImpl
import me.justlime.redeemX.state.StateManager
import org.bukkit.plugin.java.JavaPlugin

class RedeemX : JavaPlugin() {
    lateinit var redeemCodeDB: RedeemCodeDaoImpl
    private lateinit var configFile: ConfigManager
    val bot = DiscordBot(this)
    lateinit var stateManager: StateManager // Ensure StateManager is initialized before use

    override fun onEnable() {
        if (!this.dataFolder.exists()) this.dataFolder.mkdir()

        // Register and Initialize Database
        redeemCodeDB = DatabaseManager.getInstance(this).getRedeemCodeDao()
        redeemCodeDB.init()

        // Initialize StateManager
        stateManager = StateManager(this)

        // Config
        configFile = ConfigManager(this)

        // Initialize Commands with StateManager
        CommandManager(this)

        val isBotEnabled = configFile.getString("bot.enabled").equals("true", true)
        if (isBotEnabled)bot.startBot()

        logger.info("RedeemX Plugin has been enabled!")
    }

    override fun onDisable() {
        DatabaseManager.getInstance(this).closePool()
        logger.info("RedeemX Plugin has been disabled!")
        bot.stopBot()
    }
}
