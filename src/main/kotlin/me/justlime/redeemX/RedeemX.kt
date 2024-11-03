package me.justlime.redeemX

import me.justlime.redeemX.commands.CommandHandler
import me.justlime.redeemX.commands.RedeemCommand
import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.dao.RedeemCodeDaoImpl
import org.bukkit.plugin.java.JavaPlugin

class RedeemX : JavaPlugin() {
    lateinit var commandHandler: CommandHandler
    lateinit var redeemCodeDao: RedeemCodeDaoImpl
    private lateinit var databaseManager: DatabaseManager

    override fun onEnable() {
        //config
        this.saveDefaultConfig()
        commandHandler = CommandHandler(this, "rcx")
        RedeemCommand(this).rcx(commandHandler)
        commandHandler = CommandHandler(this, "redeem")
        RedeemCommand(this).redeem(commandHandler)


        // Initialize the database and DAO

        databaseManager = DatabaseManager()
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
