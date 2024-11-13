package me.justlime.redeemX

import me.justlime.redeemX.commands.CommandManager
import me.justlime.redeemX.config.ConfigManager
import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.dao.RedeemCodeDaoImpl
import me.justlime.redeemX.utilities.UtilitiesManager
import org.bukkit.plugin.java.JavaPlugin

class RedeemX : JavaPlugin() {
    lateinit var redeemCodeDB: RedeemCodeDaoImpl
    lateinit var configFile: ConfigManager


    override fun onEnable() {
        //config
        configFile = ConfigManager(this)


        //Register and Initialize
        redeemCodeDB = DatabaseManager.getInstance(this).getRedeemCodeDao()
        redeemCodeDB.createTable()
        redeemCodeDB.fetchCodes()

        UtilitiesManager(this)
        CommandManager(this)

        logger.info("RedeemX Plugin has been enabled!")
    }

    override fun onDisable() {
        DatabaseManager.getInstance(this).closePool()
        logger.info("RedeemX Plugin has been disabled!")
    }

}
