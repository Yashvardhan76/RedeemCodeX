package me.justlime.redeemX.data.local

import me.justlime.redeemX.data.models.RedeemCode
import me.justlime.redeemX.utilities.RedeemCodeService
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.sql.Timestamp

class RedeemCodeDaoImpl(private val dbManager: DatabaseManager) : RedeemCodeDao {
    lateinit var getFetchCodes: List<String>
    private lateinit var getTargetList: MutableMap<String, MutableList<String>>

    fun getTargetList(code: String): MutableList<String> {
        // Return an empty mutable list if the code is not found
        return getTargetList[code] ?: mutableListOf()
    }

    fun init() {
        fetchCodes()
        fetchTargetList()
        createTable()
    }

    private fun fetchCodes() {
        // Fetch all codes and map to their string values
        getFetchCodes = getEntireCodes().map { it.code }
    }

    private fun fetchTargetList() {
        getTargetList = mutableMapOf()

        getEntireCodes().forEach { state ->
            // Safely handle nullable targets, trim them, and add to the map
            val targetList = state.target.filterNotNull() // Remove null values from the list
                .map { it.trim() } // Trim each string
                .toMutableList() ?: mutableListOf() // Default to an empty list

            getTargetList[state.code] = targetList
        }
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
            usedBy TEXT,
            template TEXT,
            templateLocked BOOLEAN,
            storedCooldown TIMESTAMP,
            cooldown TEXT
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
        val target = redeemCode.target.toString().replace("[", "").replace("]", "").replace(",null", "")

        dbManager.getConnection()?.use { conn ->
            conn.prepareStatement(
                """
            INSERT INTO redeem_codes (code, commands, storedTime, duration, isEnabled, max_redeems, max_player, 
            permission, pin, target, usedBy, template, templateLocked, storedCooldown, cooldown)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)
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
                usedBy = EXCLUDED.usedBy,
                template = EXCLUDED.template,
                templateLocked = EXCLUDED.templateLocked,
                storedCooldown = EXCLUDED.storedCooldown,
                cooldown = EXCLUDED.cooldown
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
                statement.setString(10, target)
                statement.setString(11, usageString)
                statement.setString(12, redeemCode.template)
                statement.setBoolean(13, redeemCode.templateLocked)
                statement.setTimestamp(14, redeemCode.storedCooldown?.let { Timestamp.valueOf(it) })
                statement.setString(15, redeemCode.cooldown)

                isSuccess = statement.executeUpdate() > 0
            }
        }
        init()
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

    override fun getTemplate(template: String): RedeemCode? {
        var redeemCode: RedeemCode? = null
        dbManager.getConnection()?.use { conn ->
            conn.prepareStatement("SELECT * FROM redeem_codes WHERE template = ? LIMIT 1").use { statement ->
                statement.setString(1, template)
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
    override fun deleteEntireCodes(): Boolean {
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

    override fun getEntireCodes(): List<RedeemCode> {
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

    override fun getTemplateCodes(template: String): List<RedeemCode> {
        val templateCode = mutableListOf<RedeemCode>()
        dbManager.getConnection()?.use { conn: Connection ->
            conn.prepareStatement("SELECT * FROM redeem_codes where template = ? LIMIT 1").use { statement:
                                                                                          PreparedStatement ->
                statement.setString(1, template)
                val result = statement.executeQuery()
                while (result.next()) {
                    templateCode.add(RedeemCodeService(dbManager.plugin).mapResultSetToRedeemCode(result))
                }
            }
        }
        return templateCode
    }

}
