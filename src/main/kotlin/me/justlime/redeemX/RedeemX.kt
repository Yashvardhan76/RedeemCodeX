package me.justlime.redeemX

import me.justlime.redeemX.commands.RCXCommand
import me.justlime.redeemX.commands.RedeemCommand
import me.justlime.redeemX.commands.TabCompleterList
import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.dao.RedeemCodeDaoImpl
import me.justlime.redeemX.data.models.RedeemCode
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class RedeemX : JavaPlugin() {
    lateinit var redeemCodeDao: RedeemCodeDaoImpl
    private lateinit var databaseManager: DatabaseManager
    private val db = File(dataFolder, "redeemx.db")

    override fun onEnable() {
        //config
        this.saveDefaultConfig()
        getCommand("rcx")?.setExecutor(RCXCommand(this))
        getCommand("rcx")?.tabCompleter = TabCompleterList()
        getCommand("redeem")?.setExecutor(RedeemCommand(this))
        getCommand("redeem")?.tabCompleter = RedeemCommand(this)

        // Initialize the database and DAO

        databaseManager = DatabaseManager(db)
        redeemCodeDao = RedeemCodeDaoImpl(databaseManager)
        redeemCodeDao.createTable()

        // Register commands
//        getCommand("rcx")?.setExecutor(RedeemCommand(this))
        //tab completer register
//        getCommand("rcx")?.tabCompleter = TabCompleterList()
        logger.info("RedeemX Plugin has been enabled!")
    }


    override fun onDisable() {
        databaseManager.close()
        logger.info("RedeemX Plugin has been disabled!")
    }


}
