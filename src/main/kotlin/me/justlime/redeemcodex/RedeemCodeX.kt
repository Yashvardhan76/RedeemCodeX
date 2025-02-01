/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 *
 */

package me.justlime.redeemcodex

import me.clip.placeholderapi.metrics.bukkit.Metrics
import me.justlime.redeemcodex.api.RedeemXAPI
import me.justlime.redeemcodex.commands.CommandManager
import me.justlime.redeemcodex.data.config.ConfigManager
import me.justlime.redeemcodex.data.local.DatabaseManager
import me.justlime.redeemcodex.data.local.RedeemCodeDaoImpl
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.gui.holders.GUIHandle
import me.justlime.redeemcodex.listener.ListenerManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.sql.SQLException
import java.util.logging.Level

class RedeemCodeX : JavaPlugin() {
    lateinit var redeemCodeDB: RedeemCodeDaoImpl
    lateinit var configManager: ConfigManager
    lateinit var listenerManager: ListenerManager
    lateinit var configRepo: ConfigRepository

    override fun onEnable() {
        //To Support older version of Minecraft
        Class.forName("org.sqlite.JDBC")


        if (!this.dataFolder.exists()) this.dataFolder.mkdir()
        RedeemXAPI.initialize(this)
        configManager = ConfigManager(this)
        try {
            redeemCodeDB = DatabaseManager.getInstance(this).getRedeemCodeDao()
            this.logger.info("\u001B[32mSuccessfully initialized database\u001B[0m")
        } catch (e: SQLException) {
            this.logger.log(Level.SEVERE, "Failed to initialize database", e)
        }

        try {
            redeemCodeDB.fetch()
            this.logger.info("\u001B[32mSuccessfully fetched redeem codes from database\u001B[0m")
        } catch (e: SQLException) {
            this.logger.log(Level.SEVERE, "Failed to fetch redeem codes from database", e)
        }

        configRepo = ConfigRepository(this)
        CommandManager(this)
        listenerManager = ListenerManager(this)
        //Line
        this.logger.info("\u001B[32mRedeemX Plugin has been enabled!\u001B[0m")
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.logger.info("\u001B[32mPlacedHolderAPI Redeemed\u001B[0m")
        }
        Metrics(this, 24336)
    }

    override fun onDisable() {
        server.onlinePlayers.forEach { if (it.openInventory.topInventory.holder is GUIHandle) it.closeInventory() }
        DatabaseManager.getInstance(this).closePool()
        logger.info("RedeemX Plugin has been disabled!")
    }
}