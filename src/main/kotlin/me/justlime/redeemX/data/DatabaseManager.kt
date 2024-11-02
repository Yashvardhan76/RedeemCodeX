package me.justlime.redeemX.data

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseManager {
    private val url = "jdbc:sqlite:redeemx.db"

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
