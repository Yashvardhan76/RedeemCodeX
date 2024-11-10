package me.justlime.redeemX.data.dao

import me.justlime.redeemX.data.DatabaseManager
import me.justlime.redeemX.data.models.RedeemCode
import java.sql.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class RedeemCodeDaoImpl(private val dbManager: DatabaseManager) : RedeemCodeDao {
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

    override fun insert(redeemCode: RedeemCode): Boolean {
        var isInserted = false
        val commandsString = redeemCode.commands.entries.joinToString(",") { "${it.key}:${it.value}" }
        val usageString = redeemCode.usage.entries.joinToString(",") { "${it.key}:${it.value}" }
        val stamp: Timestamp? = if(redeemCode.storedTime != null) Timestamp.valueOf(redeemCode.storedTime) else null
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement(
                "INSERT INTO redeem_codes (code, commands, storedTime, duration, isEnabled, max_redeems, max_player, permission, pin, target, usedBy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)"
            ).use { statement: PreparedStatement ->
                statement.setString(1, redeemCode.code)
                statement.setString(2, commandsString)
                statement.setTimestamp(3, stamp)
                statement.setObject(3, redeemCode.duration)
                statement.setBoolean(4, redeemCode.isEnabled)
                statement.setInt(5, redeemCode.maxRedeems)
                statement.setInt(6, redeemCode.maxPlayers)
                statement.setString(8, redeemCode.permission)
                statement.setInt(9, redeemCode.pin)
                statement.setString(10, redeemCode.target)
                statement.setString(11, usageString)
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


    override fun get(code: String): RedeemCode? {

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
        val usageString = redeemCode.usage.entries.joinToString(",") { "${it.key}:${it.value}" }
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement(
                "UPDATE redeem_codes SET commands = ?,storedTime = ?, duration = ?, isEnabled = ?, max_redeems = ?, max_player = ?, permission = ?, pin = ?, target = ?, usedBy = ? WHERE code = ?"
            ).use { statement: PreparedStatement ->
                statement.setString(1, commandsString)
                statement.setTimestamp(2, Timestamp.valueOf(redeemCode.storedTime))
                statement.setString(3, redeemCode.duration)
                statement.setBoolean(4, redeemCode.isEnabled)
                statement.setInt(5, redeemCode.maxRedeems)
                statement.setInt(6, redeemCode.maxPlayers)
                statement.setString(7, redeemCode.permission)
                statement.setInt(8, redeemCode.pin)
                statement.setString(9, redeemCode.target)
                statement.setString(10, usageString)
                statement.setString(11, redeemCode.code)
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

    private val timeZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
    private val timeZone: ZonedDateTime = ZonedDateTime.now(timeZoneId)
    private val currenTime: LocalDateTime = timeZone.toLocalDateTime()
    override fun isExpired(code: String): Boolean {
        val redeemCode = get(code)
        val storedTime = redeemCode?.storedTime?: return false
        val duration = redeemCode.duration ?: return false
        val calcTime = calculateExpiry(storedTime, duration)
        return calcTime?.isBefore(currenTime) ?: false
    }


    private fun calculateExpiry(time: LocalDateTime, duration: String): LocalDateTime? {
        val amount = duration.dropLast(1).toIntOrNull() ?: return null
        return when (duration.takeLast(1)) {
            "s" -> time.plusSeconds(amount.toLong())
            "m" -> time.plusMinutes(amount.toLong())
            "h" -> time.plusHours(amount.toLong())
            "d" -> time.plusDays(amount.toLong())
            "mo" -> time.plusMonths(amount.toLong())
            "y" -> time.plusYears(amount.toLong())
            else -> return null
        }
    }

    override fun getAllCommands(code: String): MutableMap<Int, String>? {
        val commands = get(code)?.commands ?: return null
        if (commands.values.toString().trim().isEmpty()) return null
        return commands
    }

    override fun getCommandById(code: String, id: Int): String? {
        val commands = get(code)?.commands?.containsKey(id)?.toString() ?: return null
        if (commands.trim().isEmpty()) return null
        return commands
    }


    private fun mapResultSetToRedeemCode(result: ResultSet): RedeemCode {
        val commandsString = result.getString("commands") ?: null
        val usageString = result.getString("usedBy") ?: null
        val storedTime = if(result.getTimestamp("duration") == null) null else result.getTimestamp("duration")?.toLocalDateTime()
        val commandMap = parseToMapId(commandsString)
        val playerUsageMap = parseToMapString(usageString)

        return RedeemCode(
            code = result.getString("code"),
            commands = commandMap,
            storedTime = storedTime,
            duration = result.getString("duration"),
            isEnabled = result.getBoolean("isEnabled"),
            maxRedeems = result.getInt("max_redeems"),
            maxPlayers = result.getInt("max_player"),
            permission = result.getString("permission"),
            pin = result.getInt("pin"),
            target = result.getString("target"),
            usage = playerUsageMap,
        )
    }

    private fun parseToMapId(input: String?, separator: String = ":"): MutableMap<Int, String> {
        if (input.isNullOrBlank()) return mutableMapOf()
        val resultMap = mutableMapOf<Int, String>()

        for (entry in input.split(",")) {
            val parts = entry.split(separator)
            if (parts.size == 2) {
                val key = parts[0].toIntOrNull()
                val value = parts[1].takeIf { it.isNotBlank() }
                if (key != null && value != null) {
                    resultMap[key] = value
                }
            }
        }

        return resultMap
    }

    private fun parseToMapString(input: String?, separator: String = ":"): MutableMap<String, Int> {
        if (input.isNullOrBlank()) return mutableMapOf()
        val resultMap = mutableMapOf<String, Int>()

        for (entry in input.split(",")) {
            val parts = entry.split(separator)
            if (parts.size == 2) {
                val key = parts[0].takeIf { it.isNotBlank() }
                val value = parts[1].toIntOrNull()
                if (key != null && value != null) {
                    resultMap[key] = value
                }
            }
        }

        return resultMap
    }

}
