package me.justlime.redeemX

//import me.justlime.redeemX.bot.DiscordBot
import me.justlime.redeemX.api.RedeemXAPI
import me.justlime.redeemX.commands.CommandManager
import me.justlime.redeemX.data.config.ConfigManager
import me.justlime.redeemX.data.local.DatabaseManager
import me.justlime.redeemX.data.local.RedeemCodeDaoImpl
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.listener.ListenerManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.sql.SQLException
import java.util.logging.Level

class RedeemX : JavaPlugin() {
    lateinit var redeemCodeDB: RedeemCodeDaoImpl
    lateinit var configManager: ConfigManager
    lateinit var config: ConfigRepository
    override fun onLoad() {
        RedeemXAPI.initialize(this)
    }

    override fun onEnable() {
        Class.forName("org.sqlite.JDBC")
        if (!this.dataFolder.exists()) this.dataFolder.mkdir()
        configManager = ConfigManager(this)
        try {
            redeemCodeDB = DatabaseManager.getInstance(this).getRedeemCodeDao()
            this.logger.info("\u001B[32mSuccessfully initialized database\u001B[0m")
        } catch (e: SQLException) { this.logger.log(Level.SEVERE, "Failed to initialize database", e) }

        try {
            redeemCodeDB.fetch()
            this.logger.info("\u001B[32mSuccessfully fetched redeem codes from database\u001B[0m")
        } catch (e: SQLException) { this.logger.log(Level.SEVERE, "Failed to fetch redeem codes from database", e) }

        config = ConfigRepository(this)
        CommandManager(this)
        ListenerManager(this)
        //Line
        this.logger.info("\u001B[32mRedeemX Plugin has been enabled!\u001B[0m")
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            this.logger.info("\u001B[32mPlacedHolderAPI Redeemed\u001B[0m")
        }
    }

    override fun onDisable() {
        DatabaseManager.getInstance(this).closePool()
        logger.info("RedeemX Plugin has been disabled!")
    }
}