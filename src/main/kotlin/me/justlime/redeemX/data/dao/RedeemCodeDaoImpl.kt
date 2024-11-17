package me.justlime.redeemX.data.dao

import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.data.service.RedeemCodeService
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.sql.Timestamp

class RedeemCodeDaoImpl(private val dbManager: DatabaseManager) : RedeemCodeDao {
    lateinit var getFetchCodes: List<String>

    fun fetchCodes() {
     getFetchCodes = getAllCodes().map { it.code }
    }


    override fun createTable() {
        dbManager.getConnection()?.use { conn: Connection ->
            conn.createStatement().use { statement: Statement ->
                statement.executeUpdate(
                    """
        CREATE TABLE IF NOT EXISTS redeem_codes (
            code TEXT PRIMARY KEY,
            commands TEXT,
            storedTime TIMESTAMP,
            duration TEXT,
            isEnabled BOOLEAN,
            max_redeems INTEGER,
            max_player INTEGER,
            permission TEXT,
            pin INT,
            target TEXT,
            usedBy TEXT
        )
        """
                )
            }
        }
    }

    override fun upsert(redeemCode: RedeemCode): Boolean {
        var isSuccess = false
        val code = redeemCode.code
        val commandsString = redeemCode.commands.entries.joinToString(",") { "${it.key}:${it.value}" }
        val usageString = redeemCode.usage.entries.joinToString(",") { "${it.key}:${it.value}" }
        val stamp = redeemCode.storedTime?.let { Timestamp.valueOf(it) }

        dbManager.getConnection()?.use { conn ->
            conn.prepareStatement(
                """
            INSERT INTO redeem_codes (code, commands, storedTime, duration, isEnabled, max_redeems, max_player, permission, pin, target, usedBy)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(code) DO UPDATE SET 
                commands = EXCLUDED.commands,
                storedTime = EXCLUDED.storedTime,
                duration = EXCLUDED.duration,
                isEnabled = EXCLUDED.isEnabled,
                max_redeems = EXCLUDED.max_redeems,
                max_player = EXCLUDED.max_player,
                permission = EXCLUDED.permission,
                pin = EXCLUDED.pin,
                target = EXCLUDED.target,
                usedBy = EXCLUDED.usedBy
            """
            ).use { statement ->
                // Set parameters, starting with code, which we assume is non-null at this point
                statement.setString(1, code)
                statement.setString(2, commandsString)
                statement.setTimestamp(3, stamp)
                statement.setString(4, redeemCode.duration)
                statement.setBoolean(5, redeemCode.isEnabled)
                statement.setInt(6, redeemCode.maxRedeems)
                statement.setInt(7, redeemCode.maxPlayers)
                statement.setString(8, redeemCode.permission)
                statement.setInt(9, redeemCode.pin)
                statement.setString(10, redeemCode.target)
                statement.setString(11, usageString)

                isSuccess = statement.executeUpdate() > 0
            }
        }

        if (isSuccess) fetchCodes()
        return isSuccess
    }

    override fun get(code: String): RedeemCode? {
        var redeemCode: RedeemCode? = null
        dbManager.getConnection()?.use { conn ->
            conn.prepareStatement("SELECT * FROM redeem_codes WHERE code = ?").use { statement ->
                statement.setString(1, code)
                val result = statement.executeQuery()
                if (result.next()) {
                    redeemCode = RedeemCodeService(dbManager.plugin).mapResultSetToRedeemCode(result)
                }
            }
        }
        return redeemCode
    }

    override fun deleteByCode(code: String): Boolean {
        var isDeleted = false
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement("DELETE FROM redeem_codes WHERE code = ?").use { statement: PreparedStatement ->
                statement.setString(1, code)
                isDeleted = statement.executeUpdate() > 0
            }
        }
        return isDeleted
    }

    @Suppress("SqlWithoutWhere")
    override fun deleteAll(): Boolean {
        var isDeletedAll = false
        dbManager.getConnection()?.use { conn: Connection ->
            val sql = "delete from redeem_codes"
            conn.prepareStatement(sql).use {
                // Execute the delete operation
                val affectedRows = it.executeUpdate()
                isDeletedAll = affectedRows > 0 // Check if any rows were affected (deleted)
            }
        } ?: run {
            return isDeletedAll
        }
        return isDeletedAll
    }

    override fun getAllCodes(): List<RedeemCode> {
        val codes = mutableListOf<RedeemCode>()
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement("SELECT * FROM redeem_codes").use { statement: PreparedStatement ->
                val result = statement.executeQuery()
                while (result.next()) {
                    codes.add(RedeemCodeService(dbManager.plugin).mapResultSetToRedeemCode(result))
                }
            }
        }
        return codes
    }






}
