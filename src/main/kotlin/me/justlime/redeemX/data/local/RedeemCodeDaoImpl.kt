package me.justlime.redeemX.data.local

import me.justlime.redeemX.enums.JProperty
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.utilities.Converter
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

class RedeemCodeDaoImpl(private val dbManager: DatabaseManager) : RedeemCodeDao {
    private val getCachedCodes: MutableList<String> = mutableListOf()
    private val getCachedTargetList: MutableMap<String, MutableList<String>> = mutableMapOf() //<code,list of targets>
    private val getCachedUsageList: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()
    private val converter = Converter()

    init {
        createTable()
        fetch()
    }

    override fun createTable() {
        dbManager.getConnection()?.use { conn: Connection ->
            conn.createStatement().use { statement: Statement ->
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS redeem_codes (
                        ${JProperty.CODE.property} TEXT PRIMARY KEY,
                        ${JProperty.ENABLED.property} BOOLEAN,
                        ${JProperty.TEMPLATE.property} TEXT,
                        ${JProperty.LOCKED.property} BOOLEAN,
                        ${JProperty.DURATION.property} TEXT,
                        ${JProperty.COOLDOWN.property} TEXT,
                        ${JProperty.PERMISSION.property} TEXT,
                        ${JProperty.PIN.property} INTEGER,
                        ${JProperty.REDEMPTION.property} INTEGER,
                        ${JProperty.LIMIT.property} INTEGER,
                        ${JProperty.USED_BY.property} TEXT,
                        ${JProperty.VALID_FROM.property} TIMESTAMP,
                        ${JProperty.LAST_REDEEMED.property} TIMESTAMP,
                        ${JProperty.TARGET.property} TEXT,
                        ${JProperty.COMMANDS.property} TEXT,
                        ${JProperty.CREATED.property} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        ${JProperty.MODIFIED.property} TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """
                )
            }
        }
    }

    override fun upsertCode(redeemCode: RedeemCode): Boolean {
        var isSuccess = false
        if (redeemCode.code.isBlank() || redeemCode.code.isEmpty()) return false

        dbManager.getConnection()?.use { conn ->
            val columns = JProperty.entries.map { it.property }
            val insertColumns = columns.filter { it != JProperty.CREATED.property }
            val placeholders = insertColumns.joinToString(", ") { "?" }
            val updateFields = JProperty.entries
                .filter { it != JProperty.CODE } // Exclude the primary key from updates
                .joinToString(", ") { column ->
                    if (column == JProperty.MODIFIED) {
                        "${column.property} = CURRENT_TIMESTAMP"
                    } else {
                        "${column.property} = EXCLUDED.${column.property}"
                    }
                }


            val sql = """
            INSERT INTO redeem_codes (${insertColumns.joinToString(", ")})
            VALUES ($placeholders)
            ON CONFLICT(${JProperty.CODE.property}) DO UPDATE SET 
                $updateFields
            """

            conn.prepareStatement(sql).use { statement ->
                val mappedData = converter.mapRedeemCodeToDatabase(redeemCode)

                statement.setString(1, mappedData.code)
                statement.setBoolean(2, mappedData.isEnabled)
                statement.setString(3, mappedData.template)
                statement.setBoolean(4, mappedData.templateLocked)
                statement.setString(5, mappedData.duration)
                statement.setString(6, mappedData.cooldown)
                statement.setString(7, mappedData.permission)
                statement.setInt(8, mappedData.pin)
                statement.setInt(9, mappedData.redemption)
                statement.setInt(10, mappedData.limit)
                statement.setString(11, mappedData.usedBy)
                statement.setTimestamp(12, mappedData.validFrom)
                statement.setString(13, mappedData.lastRedeemed)
                statement.setString(14, mappedData.target)
                statement.setString(15, mappedData.commands)

                isSuccess = statement.executeUpdate() > 0
            }
        }
        fetch() // Refresh in-memory data if needed
        return isSuccess
    }


    override fun upsertCodes(redeemCodes: List<RedeemCode>): Boolean {
        var isSuccess = false
        if (redeemCodes.isEmpty()) return false

        dbManager.getConnection()?.use { conn ->
            val columns = JProperty.entries.map { it.property }
            val insertColumns = columns.filter { it != JProperty.CREATED.property }
            val placeholders = insertColumns.joinToString(", ") { "?" }
            val updateFields = JProperty.entries
                .filter { it != JProperty.CODE } // Exclude the primary key from updates
                .joinToString(", ") { column ->
                    if (column == JProperty.MODIFIED) {
                        "${column.property} = CURRENT_TIMESTAMP"
                    } else {
                        "${column.property} = EXCLUDED.${column.property}"
                    }
                }


            val sql = """
            INSERT INTO redeem_codes (${insertColumns.joinToString(", ")})
            VALUES ($placeholders)
            ON CONFLICT(${JProperty.CODE.property}) DO UPDATE SET 
                $updateFields
            """

            conn.prepareStatement(sql).use { statement ->
                for (redeemCode in redeemCodes) {
                    val mappedData = converter.mapRedeemCodeToDatabase(redeemCode)

                    statement.setString(1, mappedData.code)
                    statement.setBoolean(2, mappedData.isEnabled)
                    statement.setString(3, mappedData.template)
                    statement.setBoolean(4, mappedData.templateLocked)
                    statement.setString(5, mappedData.duration)
                    statement.setString(6, mappedData.cooldown)
                    statement.setString(7, mappedData.permission)
                    statement.setInt(8, mappedData.pin)
                    statement.setInt(9, mappedData.redemption)
                    statement.setInt(10, mappedData.limit)
                    statement.setString(11, mappedData.usedBy)
                    statement.setTimestamp(12, mappedData.validFrom)
                    statement.setString(13, mappedData.lastRedeemed)
                    statement.setString(14, mappedData.target)
                    statement.setString(15, mappedData.commands)

                    statement.addBatch()
                }

                val results = statement.executeBatch()
                isSuccess = results.all { it > 0 } // Check if all updates/inserts were successful
            }
        }
        fetch() // Refresh in-memory data if needed
        return isSuccess
    }


    override fun get(code: String): RedeemCode? {
        return fetchRedeemCodes("SELECT * FROM redeem_codes WHERE code = ?", code).firstOrNull()
    }

    override fun fetch() {
        getEntireCodes().forEach { state ->
            getCachedTargetList[state.code] = state.target
            getCachedUsageList[state.code] = state.usedBy
            getCachedCodes.add(state.code)
        }
    }

    override fun getCachedCodes(): List<String> {
        return getCachedCodes
    }

    override fun getCachedTargets(): MutableMap<String, MutableList<String>> {
        return getCachedTargetList
    }

    override fun getCachedUsages(): MutableMap<String, MutableMap<String, Int>> {
        return getCachedUsageList
    }

    override fun getByProperty(property: JProperty, value: String): List<RedeemCode> {
        return fetchRedeemCodes("SELECT * FROM redeem_codes WHERE $property.property = ?", value)
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
            val sql = "DELETE FROM redeem_codes"
            conn.prepareStatement(sql).use {
                val affectedRows = it.executeUpdate()
                isDeletedAll = affectedRows > 0
            }
        }
        return isDeletedAll
    }

    override fun getEntireCodes(): List<RedeemCode> {
        return fetchRedeemCodes("SELECT * FROM redeem_codes")
    }

    override fun getTemplateCodes(template: String): List<RedeemCode> {
        return fetchRedeemCodes("SELECT * FROM redeem_codes WHERE template = ?", template)
    }

    private fun fetchRedeemCodes(query: String, vararg params: Any): List<RedeemCode> {
        val codes = mutableListOf<RedeemCode>()
        dbManager.getConnection()?.use { conn ->
            conn.prepareStatement(query).use { statement ->
                params.forEachIndexed { index, param ->
                    statement.setObject(index + 1, param)
                }
                val result = statement.executeQuery()
                while (result.next()) {
                    codes.add(converter.mapResultSetToRedeemCode(result))
                }
            }
        }
        return codes
    }

}
