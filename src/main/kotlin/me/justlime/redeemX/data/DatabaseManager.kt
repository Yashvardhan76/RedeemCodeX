package me.justlime.redeemX.data

import me.justlime.redeemX.RedeemX
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseManager(databaseFile: File) {
    private val url = "jdbc:sqlite:$databaseFile"

    fun getConnection(): Connection? {
        return try {
            DriverManager.getConnection(url)
        } catch (e: SQLException) {
            println("Failed to connect to the SQLite database: ${e.message}")
            null
        }
    }

    fun close() {
        getConnection()?.close()
    }
}
