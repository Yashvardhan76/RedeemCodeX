package me.justlime.redeemX.data.dao

import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.models.RedeemCode
import java.sql.*
import java.time.LocalDateTime

class RedeemCodeDaoImpl(private val dbManager: DatabaseManager) : RedeemCodeDao {
    override fun createTable() {
        dbManager.getConnection()?.use { conn: Connection ->
            conn.createStatement().use { statement: Statement ->
                statement.executeUpdate(
                    """
        CREATE TABLE IF NOT EXISTS redeem_codes (
            code TEXT PRIMARY KEY,
            commands TEXT,
            duration TEXT,
            isEnabled BOOLEAN,
            max_redeems INTEGER,
            max_player INTEGER,
            max_redeems_per_player INTEGER,
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

    override fun insert(redeemCode: RedeemCode): Boolean {
        var isInserted = false
        val commandsString = redeemCode.commands.entries.joinToString(",") { "${it.key}:${it.value}" }
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement(
                "INSERT INTO redeem_codes (code, commands, duration, isEnabled, max_redeems, max_player, max_redeems_per_player, permission, pin, target, usedBy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)"
            ).use { statement: PreparedStatement ->
                statement.setString(1, redeemCode.code)
                statement.setString(2, commandsString)
                statement.setObject(3, redeemCode.duration)
                statement.setBoolean(4, redeemCode.isEnabled)
                statement.setInt(5, redeemCode.max_redeems)
                statement.setInt(6, redeemCode.max_player)
                statement.setInt(7, redeemCode.max_redeems_per_player)
                statement.setString(8, redeemCode.permission)
                statement.setInt(9, redeemCode.pin)
                statement.setString(10, redeemCode.target)
                statement.setString(11, redeemCode.usedBy)
                try {
                    isInserted = statement.executeUpdate() > 0
                } catch (e: SQLException) {
                    if (e.message?.contains("UNIQUE constraint failed") == true) {
                        return false
                    } else {
                        throw e
                    }
                }
            }
        }
        return isInserted
    }


    override fun getByCode(code: String): RedeemCode? {

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


    override fun update(redeemCode: RedeemCode): Boolean {
        var isUpdated = false
        val commandsString = redeemCode.commands.entries.joinToString(",") { "${it.key}:${it.value}" }
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement(
                "UPDATE redeem_codes SET commands = ?, duration = ?, isEnabled = ?, max_redeems = ?, max_player = ?,max_redeems_per_player = ?, permission = ?, pin = ?, target = ? WHERE code = ?"
            ).use { statement: PreparedStatement ->
                statement.setString(1, commandsString)
                statement.setString(2, redeemCode.duration)
                statement.setBoolean(3, redeemCode.isEnabled)
                statement.setInt(4, redeemCode.max_redeems)
                statement.setInt(5, redeemCode.max_player)
                statement.setInt(6, redeemCode.max_redeems_per_player)
                statement.setString(7, redeemCode.permission)
                statement.setInt(8, redeemCode.pin)
                statement.setString(9, redeemCode.target)
                statement.setString(10, redeemCode.code)
                isUpdated = statement.executeUpdate() > 0
            }
        }
        return isUpdated
    }

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
                    codes.add(mapResultSetToRedeemCode(result))
                }
            }
        }
        return codes
    }

    override fun isExpired(code: String): Boolean {
        val redeemCode = getByCode(code) ?: return true  // Return true if code doesn't exist
        val expiry = redeemCode.duration ?: return false    // If no expiry is set, consider it as not expired
//        return expiry.isBefore(LocalDateTime.now())       // Check if expiry is before the current time
        return false
    }

    override fun addCommand(code: String, command: String): Boolean {
        val redeemCode = getByCode(code) ?: return false
        val id = (redeemCode.commands.keys.maxOrNull() ?: 0) + 1
        val updatedCommands = redeemCode.commands + (id to command)
        val commandsString = updatedCommands.entries.joinToString(",") { "${it.key}:${it.value}" }
        var isUpdated = false
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement(
                "UPDATE redeem_codes SET commands = ? WHERE code = ?"
            ).use { statement: PreparedStatement ->
                statement.setString(1, commandsString)
                statement.setString(2, code)
                isUpdated = statement.executeUpdate() > 0
            }
        }
        return isUpdated
    }

    override fun setCommand(code: String, command: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun setCommandById(code: String, id: Int, command: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAllCommands(code: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCommandById(code: String, id: Int): RedeemCode? {
        TODO("Not yet implemented")
    }

    override fun deleteCommandById(code: String, id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteAllCommands(code: String): Boolean {
        TODO("Not yet implemented")
    }


    private fun calculateExpiry(duration: String): LocalDateTime? {
        val now = LocalDateTime.now()
        val amount = duration.dropLast(1).toIntOrNull() ?: return null
        return when (duration.takeLast(1)) {
            "s" -> now.plusSeconds(amount.toLong())
            "m" -> now.plusMinutes(amount.toLong())
            "h" -> now.plusHours(amount.toLong())
            "d" -> now.plusDays(amount.toLong())
            "mo" -> now.plusMonths(amount.toLong())
            "y" -> now.plusYears(amount.toLong())
            else -> return null
        }
    }

    private fun mapResultSetToRedeemCode(result: ResultSet): RedeemCode {
        val commandsString = result.getString("commands")
            // Convert "1:say hello,2:eco bal 1000" back to a map
        val commandsMap = commandsString.split(",")
            .mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toIntOrNull()
                    val command = parts[1].takeIf { it.isNotBlank() }
                    if (id != null && command != null) {
                        id to command
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            .toMap()

        return RedeemCode(
            code = result.getString("code"),
            commands = commandsMap,
            duration = result.getString("duration"),
            isEnabled = result.getBoolean("isEnabled"),
            max_redeems = result.getInt("max_redeems"),
            max_player = result.getInt("max_player"),
            max_redeems_per_player = result.getInt("max_redeems_per_player"),
            permission = result.getString("permission"),
            pin = result.getInt("pin"),
            target = result.getString("target"),
            usedBy = result.getString("usedBy"),
        )
    }

}
