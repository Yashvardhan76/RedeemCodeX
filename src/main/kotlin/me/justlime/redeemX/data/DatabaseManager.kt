package me.justlime.redeemX.data

import java.sql.Connection
import java.sql.DriverManager

class DatabaseManager {
    private val url = "jdbc:mysql://<host>:<port>/<database>?useSSL=false"
    private val user = "<username>"
    private val password = "<password>"

    fun getConnection(): Connection? {
        return DriverManager.getConnection(url, user, password)
    }

    fun close() {
        getConnection()?.close()
    }
}
