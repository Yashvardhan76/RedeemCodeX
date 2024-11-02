package me.justlime.redeemX.data.dao

import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.models.RedeemCode
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

class RedeemCodeDaoImpl(private val dbManager: DatabaseManager) : RedeemCodeDao {

    override fun createTable() {
        dbManager.getConnection()?.use { conn: Connection ->
            conn.createStatement().use { statement: Statement ->
                statement.executeUpdate(
                    """
        CREATE TABLE IF NOT EXISTS redeem_codes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            code TEXT UNIQUE,
            commands TEXT,
            maxRedeems INTEGER,
            maxPerPlayer INTEGER,
            isEnabled BOOLEAN,
            expiry TEXT,
            permission TEXT,
            secureCode TEXT,
            specificPlayerId TEXT
        )
        """
                )
            }
        }
    }

    override fun insert(redeemCode: RedeemCode): Boolean {
        var isInserted = false
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement(
                "INSERT INTO redeem_codes (code, commands, maxRedeems, maxPerPlayer, isEnabled, expiry, permission, secureCode, specificPlayerId, guiEditMode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            ).use { statement: PreparedStatement ->
                statement.setString(1, redeemCode.code)
                statement.setString(2, redeemCode.commands.joinToString(","))
                statement.setInt(3, redeemCode.maxRedeems)
                statement.setInt(4, redeemCode.maxPerPlayer)
                statement.setBoolean(5, redeemCode.isEnabled)
                statement.setObject(6, redeemCode.expiry)
                statement.setString(7, redeemCode.permission)
                statement.setString(8, redeemCode.secureCode)
                statement.setString(9, redeemCode.specificPlayerId)
                isInserted = statement.executeUpdate() > 0
            }
        }
        return isInserted
    }

    override fun findById(id: Int): RedeemCode? {
        var redeemCode: RedeemCode? = null
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement("SELECT * FROM redeem_codes WHERE id = ?").use { statement: PreparedStatement ->
                statement.setInt(1, id)
                val result = statement.executeQuery()
                if (result.next()) {
                    redeemCode = mapResultSetToRedeemCode(result)
                }
            }
        }
        return redeemCode
    }

    override fun findByCode(code: String): RedeemCode? {
        var redeemCode: RedeemCode? = null
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement("SELECT * FROM redeem_codes WHERE code = ?").use { statement: PreparedStatement ->
                statement.setString(1, code)
                val result = statement.executeQuery()
                if (result.next()) {
                    redeemCode = mapResultSetToRedeemCode(result)
                }
            }
        }
        return redeemCode
    }

    override fun update(redeemCode: RedeemCode): Boolean {
        var isUpdated = false
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement(
                "UPDATE redeem_codes SET commands = ?, maxRedeems = ?, maxPerPlayer = ?, isEnabled = ?, expiry = ?, permission = ?, secureCode = ?, specificPlayerId = ?, guiEditMode = ? WHERE code = ?"
            ).use { statement: PreparedStatement ->
                statement.setString(1, redeemCode.commands.joinToString(","))
                statement.setInt(2, redeemCode.maxRedeems)
                statement.setInt(3, redeemCode.maxPerPlayer)
                statement.setBoolean(4, redeemCode.isEnabled)
                statement.setObject(5, redeemCode.expiry)
                statement.setString(6, redeemCode.permission)
                statement.setString(7, redeemCode.secureCode)
                statement.setString(8, redeemCode.specificPlayerId)
                statement.setString(10, redeemCode.code)
                isUpdated = statement.executeUpdate() > 0
            }
        }
        return isUpdated
    }

    override fun deleteById(id: Int): Boolean {
        var isDeleted = false
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement("DELETE FROM redeem_codes WHERE id = ?").use { statement: PreparedStatement ->
                statement.setInt(1, id)
                isDeleted = statement.executeUpdate() > 0
            }
        }
        return isDeleted
    }

    override fun getAllCodes(): List<RedeemCode> {
        val codes = mutableListOf<RedeemCode>()
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement("SELECT * FROM redeem_codes").use { statement: PreparedStatement ->
                val result = statement.executeQuery()
                while (result.next()) {
                    codes.add(mapResultSetToRedeemCode(result))
                }
            }
        }
        return codes
    }

    private fun mapResultSetToRedeemCode(result: ResultSet): RedeemCode {
        return RedeemCode(
            id = result.getInt("id"),
            code = result.getString("code"),
            commands = result.getString("commands").split(","),
            maxRedeems = result.getInt("maxRedeems"),
            maxPerPlayer = result.getInt("maxPerPlayer"),
            isEnabled = result.getBoolean("isEnabled"),
            expiry = result.getTimestamp("expiry")?.toLocalDateTime(),
            permission = result.getString("permission"),
            secureCode = result.getString("secureCode"),
            specificPlayerId = result.getString("specificPlayerId"),
        )
    }
}
