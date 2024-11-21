package me.justlime.redeemX

import me.justlime.redeemX.commands.CommandManager
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.dao.RedeemCodeDaoImpl
import me.justlime.redeemX.state.StateManager
import org.bukkit.plugin.java.JavaPlugin

class RedeemX : JavaPlugin() {
    lateinit var redeemCodeDB: RedeemCodeDaoImpl
    lateinit var configFile: ConfigManager
    lateinit var stateManager: StateManager // Ensure StateManager is initialized before use

    override fun onEnable() {
        if(!this.dataFolder.exists()) this.dataFolder.mkdir()

        // Register and Initialize Database
        redeemCodeDB = DatabaseManager.getInstance(this).getRedeemCodeDao()
        redeemCodeDB.init()

        // Initialize StateManager
        stateManager = StateManager(this)

        // Config
        configFile = ConfigManager(this)

        // Initialize Commands with StateManager
        CommandManager(this)

        logger.info("RedeemX Plugin has been enabled!")
    }

    override fun onDisable() {
        DatabaseManager.getInstance(this).closePool()
        logger.info("RedeemX Plugin has been disabled!")
    }
}
