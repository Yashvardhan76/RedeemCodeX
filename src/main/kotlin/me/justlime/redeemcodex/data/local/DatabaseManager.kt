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


package me.justlime.redeemcodex.data.local

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.data.repository.ConfigRepository
import java.io.File
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Logger

class DatabaseManager(val plugin: RedeemCodeX) {

    private val databaseFile = File(plugin.dataFolder, "redeemx.db")
    private val configRepo = ConfigRepository(plugin)
    private val hikariDataSource: HikariDataSource

    init {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
            maximumPoolSize = configRepo.getConfigValue("maximum-pool-size").toIntOrNull() ?: 5
            isAutoCommit = true
            connectionTestQuery = "SELECT 1"
        }
        // Suppress all HikariCP logs
        hikariDataSource = HikariDataSource(config)

        // Initialize tables or any required setup here
        try {
            getRedeemCodeDao().createTable()
        } catch (e: SQLException) {
            Logger.getLogger(javaClass.name).severe("Failed to initialize database: ${e.message}")
        }
    }

    companion object {
        private var instance: DatabaseManager? = null

        fun getInstance(plugin: RedeemCodeX): DatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: DatabaseManager(plugin).also { instance = it }
            }
        }
    }

    fun getConnection(): Connection? {
        return try {
            hikariDataSource.connection
        } catch (e: SQLException) {
            plugin.logger.info("Failed to get a connection from the pool: ${e.message}")
            null
        }
    }

    fun closePool() {
        hikariDataSource.close()
    }

    fun getRedeemCodeDao(): RedeemCodeDaoImpl {
        return RedeemCodeDaoImpl(this)
    }
}
