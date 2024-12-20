package me.justlime.redeemX

//import me.justlime.redeemX.bot.DiscordBot
import me.justlime.redeemX.api.RedeemXAPI
import me.justlime.redeemX.commands.CommandManager
import me.justlime.redeemX.data.config.ConfigManager
import me.justlime.redeemX.data.local.DatabaseManager
import me.justlime.redeemX.data.local.RedeemCodeDaoImpl
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.plugin.java.JavaPlugin

class RedeemX : JavaPlugin() {
    lateinit var redeemCodeDB: RedeemCodeDaoImpl
    lateinit var service: RedeemCodeService
    lateinit var configManager: ConfigManager
    lateinit var config: ConfigRepository
    override fun onLoad() {
        logger.info("RedeemX Plugin has been loaded!")
    }
    override fun onEnable() {
        if (!this.dataFolder.exists()) this.dataFolder.mkdir()
        configManager = ConfigManager(this)
        redeemCodeDB = DatabaseManager.getInstance(this).getRedeemCodeDao()
        redeemCodeDB.fetch()
        config = ConfigRepository(this)
        service = RedeemCodeService()
        RedeemXAPI.initialize(this)
        CommandManager(this)
        logger.info("RedeemX Plugin has been enabled!")
    }

    override fun onDisable() {
        DatabaseManager.getInstance(this).closePool()
//        bot.stopBot()
        logger.info("RedeemX Plugin has been disabled!")
    }
}