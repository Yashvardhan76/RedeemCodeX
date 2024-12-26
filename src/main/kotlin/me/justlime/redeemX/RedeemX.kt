package me.justlime.redeemX

//import me.justlime.redeemX.bot.DiscordBot
import me.justlime.redeemX.api.RedeemXAPI
import me.justlime.redeemX.commands.CommandManager
import me.justlime.redeemX.data.config.ConfigManager
import me.justlime.redeemX.data.local.DatabaseManager
import me.justlime.redeemX.data.local.RedeemCodeDaoImpl
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import java.util.logging.Logger

class RedeemX : JavaPlugin() {
    lateinit var redeemCodeDB: RedeemCodeDaoImpl
    lateinit var service: RedeemCodeService
    lateinit var configManager: ConfigManager
    lateinit var config: ConfigRepository
    override fun onLoad() {
        RedeemXAPI.initialize(this)
        logger.info("RedeemX Plugin has been loaded!")
    }
    override fun onEnable() {
        Class.forName("org.sqlite.JDBC")
        if (!this.dataFolder.exists()) this.dataFolder.mkdir()
        configManager = ConfigManager(this)
        redeemCodeDB = DatabaseManager.getInstance(this).getRedeemCodeDao()
        redeemCodeDB.fetch()
        config = ConfigRepository(this)
        service = RedeemCodeService()
        CommandManager(this)
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