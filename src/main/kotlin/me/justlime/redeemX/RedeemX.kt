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
    private lateinit var stateManager: StateManager // Ensure StateManager is initialized before use

    override fun onEnable() {
        // Initialize StateManager
        stateManager = StateManager()

        // Config
        configFile = ConfigManager(this, stateManager)

        // Register and Initialize
        redeemCodeDB = DatabaseManager.getInstance(this).getRedeemCodeDao()
        redeemCodeDB.createTable()
        redeemCodeDB.fetchCodes()

        // Initialize Commands with StateManager
        CommandManager(this, stateManager)

        logger.info("RedeemX Plugin has been enabled!")
    }

    override fun onDisable() {
        DatabaseManager.getInstance(this).closePool()
        logger.info("RedeemX Plugin has been disabled!")
    }
}
