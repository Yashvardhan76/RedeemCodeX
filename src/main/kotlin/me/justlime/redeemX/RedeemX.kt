package me.justlime.redeemX

import me.justlime.redeemX.commands.RedeemCommand
import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.dao.RedeemCodeDaoImpl
import org.bukkit.plugin.java.JavaPlugin

class RedeemX : JavaPlugin() {
    lateinit var redeemCodeDao: RedeemCodeDaoImpl
    lateinit var databaseManager: DatabaseManager

    override fun onEnable() {
        // Initialize the database and DAO
        databaseManager = DatabaseManager()
        redeemCodeDao = RedeemCodeDaoImpl(databaseManager)
        redeemCodeDao.createTable()

        // Register commands
        getCommand("rcx")?.setExecutor(RedeemCommand(this))

        logger.info("RedeemX Plugin has been enabled!")
    }

    override fun onDisable() {
        databaseManager.close()
        logger.info("RedeemX Plugin has been disabled!")
    }
}
