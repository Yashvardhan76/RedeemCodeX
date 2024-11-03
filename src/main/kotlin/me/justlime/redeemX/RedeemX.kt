package me.justlime.redeemX

import me.justlime.redeemX.commands.SubCommand
import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.dao.RedeemCodeDaoImpl
import org.bukkit.plugin.java.JavaPlugin

class RedeemX : JavaPlugin() {
    lateinit var redeemCodeDao: RedeemCodeDaoImpl
    private lateinit var databaseManager: DatabaseManager
    lateinit var rootCommand: SubCommand

    override fun onEnable() {
        //config
        this.saveDefaultConfig()
        rootCommand = SubCommand("rxc") { sender, args ->
            sender.sendMessage("§aRedeemX command")
            true
        }

        // Adding nested subcommands with automatic permissions

        rootCommand.addSubCmd("modify", usePermission = true) { sender, args ->
            sender.sendMessage("§aModify command executed")
            true
        }.addSubCmd("perms", usePermission = true) { sender, args ->
            sender.sendMessage("§aPermissions command executed")
            true
        }

        // Initialize the database and DAO

        databaseManager = DatabaseManager()
        redeemCodeDao = RedeemCodeDaoImpl(databaseManager)
        redeemCodeDao.createTable()

        // Register commands
//        getCommand("rcx")?.setExecutor(RedeemCommand(this))
        //tab completer register
//        getCommand("rcx")?.tabCompleter = TabCompleterList()
        getCommand("rxc")?.setExecutor { sender, _, _, args ->
            rootCommand.execute(sender, args.toList())
        }
        logger.info("RedeemX Plugin has been enabled!")
    }

    override fun onDisable() {
        databaseManager.close()
        logger.info("RedeemX Plugin has been disabled!")
    }
}
