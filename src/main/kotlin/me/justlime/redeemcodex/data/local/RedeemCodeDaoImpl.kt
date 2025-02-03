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

import me.justlime.redeemcodex.api.RedeemXAPI
import me.justlime.redeemcodex.enums.JProperty
import me.justlime.redeemcodex.models.RedeemCode
import me.justlime.redeemcodex.models.RedeemCodeDatabase
import me.justlime.redeemcodex.utilities.Converter
import org.bukkit.Bukkit
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

class RedeemCodeDaoImpl(private val dbManager: DatabaseManager) : RedeemCodeDao {
    private val getCachedCodes: MutableList<String> = mutableListOf()
    private val getCachedTargetList: MutableMap<String, MutableList<String>> = mutableMapOf() //<code,list of targets>
    private val getCachedUsageList: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()

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
                        ${JProperty.SYNC.property} BOOLEAN,
                        ${JProperty.DURATION.property} TEXT,
                        ${JProperty.COOLDOWN.property} TEXT,
                        ${JProperty.PERMISSION.property} TEXT,
                        ${JProperty.PIN.property} INTEGER,
                        ${JProperty.REDEMPTION.property} INTEGER,
                        ${JProperty.PLAYER_LIMIT.property} INTEGER,
                        ${JProperty.USED_BY.property} TEXT,
                        ${JProperty.VALID_FROM.property} TIMESTAMP,
                        ${JProperty.LAST_REDEEMED.property} TIMESTAMP,
                        ${JProperty.TARGET.property} TEXT,
                        ${JProperty.COMMANDS.property} TEXT,
                        ${JProperty.REWARDS.property} TEXT,
                        ${JProperty.Message.property} TEXT,
                        ${JProperty.Sound.property} TEXT,
                        ${JProperty.MODIFIED.property} TIMESTAMP,
                        created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """
                )
            }
        }
    }

    //Using if (existed) instead ON because of support lower minecraft version e.g. 1.8.9
    override fun upsertCode(redeemCode: RedeemCode): Boolean {
        if (redeemCode.code.isBlank()) return false

        var isSuccess = false
        dbManager.getConnection()?.use { conn ->
            val selectSql = "SELECT 1 FROM redeem_codes WHERE ${JProperty.CODE.property} = ?"
            val updateSql = """
                UPDATE redeem_codes 
                SET ${JProperty.entries.joinToString(", ") { "${it.property} = ?" }}
                WHERE ${JProperty.CODE.property} = ?
                """.trimIndent()
            val insertSql = """
                INSERT INTO redeem_codes (${JProperty.entries.joinToString(", ") { it.property }})
                VALUES (${JProperty.entries.joinToString(", ") { "?" }})
            """
            val exists = conn.prepareStatement(selectSql).use { statement ->
                statement.setString(1, redeemCode.code)
                statement.executeQuery().next()
            }

            if (exists) {
                // Update existing entry

                conn.prepareStatement(updateSql).use { statement ->
                    setStatementParameters(statement, redeemCode)
                    statement.setString(JProperty.entries.size + 1, redeemCode.code)
                    isSuccess = statement.executeUpdate() > 0
                }
            } else {
                // Insert new entry
                conn.prepareStatement(insertSql).use { statement ->
                    setStatementParameters(statement, redeemCode)

                    isSuccess = statement.executeUpdate() > 0
                }
            }
        }
        fetch() // Refresh in-memory data if needed
        return isSuccess
    }

    override fun upsertCodes(redeemCodes: List<RedeemCode>): Boolean {

        if (redeemCodes.isEmpty()) return false
        val existingCodes = fetchExistingCodes(redeemCodes.map { it.code })// Fetch all codes that exist in the DB

        var isSuccess = false
        dbManager.getConnection()?.use { conn ->
            val updateSql = """
            UPDATE redeem_codes
            SET ${JProperty.entries.joinToString(", ") { "${it.property} = ?" }}
            WHERE ${JProperty.CODE.property} = ?
        """.trimIndent()

            val insertSql = """
            INSERT INTO redeem_codes (${JProperty.entries.joinToString(", ") { it.property }})
            VALUES (${JProperty.entries.joinToString(", ") { "?" }})
        """.trimIndent()

            conn.autoCommit = false // Disable auto-commit for batch processing

            try {
                conn.prepareStatement(updateSql).use { updateStatement ->
                    conn.prepareStatement(insertSql).use { insertStatement ->
                        redeemCodes.forEach { redeemCode ->
                            val exists = existingCodes.contains(redeemCode.code)

                            if (exists) {
                                // Add to the UPDATE batch
                                setStatementParameters(updateStatement, redeemCode)
                                updateStatement.setString(JProperty.entries.size + 1, redeemCode.code) // Bind primary key for WHERE clause
                                updateStatement.addBatch()
                            } else {
                                // Add to the INSERT batch
                                setStatementParameters(insertStatement, redeemCode)
                                insertStatement.addBatch()
                            }
                        }

                        // Execute batches
                        val insertResults = insertStatement.executeBatch()
                        val updateResults = updateStatement.executeBatch()

                        conn.commit() // Commit transaction

                        // Check if all operations were successful
                        isSuccess = insertResults.all { it >= 0 } && updateResults.all { it >= 0 }
                    }
                }
            } catch (e: Exception) {
                conn.rollback() // Rollback transaction on error
                e.printStackTrace()
            } finally {
                conn.autoCommit = true // Restore auto-commit
            }
        }
        fetch()
        return isSuccess
    }

    private fun fetchExistingCodes(codes: List<String>): Set<String> {
        if (codes.isEmpty()) return emptySet()

        val placeholders = codes.joinToString(",") { "?" }
        val query = "SELECT ${JProperty.CODE.property} FROM redeem_codes WHERE ${JProperty.CODE.property} IN ($placeholders)" // Use property

        return dbManager.getConnection()?.use { conn ->  // Use dbManager for connection
            conn.prepareStatement(query).use { statement ->
                codes.forEachIndexed { index, code ->
                    statement.setString(index + 1, code) // Set parameters individually
                }

                val resultSet = statement.executeQuery()
                val foundCodes = mutableSetOf<String>()
                while (resultSet.next()) {
                    foundCodes.add(resultSet.getString(JProperty.CODE.property)) // Use property for retrieval
                }
                foundCodes
            }
        } ?: emptySet() // Handle null connection
    }


    private fun setStatementParameters(statement: PreparedStatement, redeemCode: RedeemCode) {
        val mappedData: RedeemCodeDatabase = Converter.mapRedeemCodeToDatabase(redeemCode)
        statement.setString(1, mappedData.code)
        statement.setBoolean(2, mappedData.enabled)
        statement.setString(3, mappedData.template)
        statement.setBoolean(4, mappedData.sync)
        statement.setString(5, mappedData.duration)
        statement.setString(6, mappedData.cooldown)
        statement.setString(7, mappedData.permission)
        statement.setInt(8, mappedData.pin)
        statement.setInt(9, mappedData.redemption)
        statement.setInt(10, mappedData.playerLimit)
        statement.setString(11, mappedData.usedBy)
        statement.setTimestamp(12, mappedData.validFrom)
        statement.setString(13, mappedData.lastRedeemed)
        statement.setString(14, mappedData.target)
        statement.setString(15, mappedData.commands)
        statement.setString(16, mappedData.rewards)
        statement.setString(17, mappedData.messages)
        statement.setString(18, mappedData.sound)
        statement.setTimestamp(19, mappedData.last_modified)
    }

    override fun get(code: String): RedeemCode? {
        return fetchRedeemCodes("SELECT * FROM redeem_codes WHERE code = ?", code).firstOrNull()
    }

    override fun lookUpCodes(codes: Set<String>): Set<String> {
        if (codes.isEmpty()) return emptySet()

        val batchSize = 999
        val result = mutableSetOf<String>()

        codes.chunked(batchSize).forEach { batch ->
            val placeholders = batch.joinToString(",") { "?" }
            val query = "SELECT code FROM redeem_codes WHERE code IN ($placeholders)"
            result += fetchRedeemCodes(query, *batch.toTypedArray()).map { it.code }
        }

        return result
    }

    override fun fetch() {
        Bukkit.getScheduler().runTaskAsynchronously(RedeemXAPI.getPlugin(), Runnable {
            getCachedCodes.clear()
            getAllCodes().forEach { state ->
                getCachedTargetList[state.code] = state.target
                getCachedUsageList[state.code] = state.usedBy
                getCachedCodes.add(state.code)
            }
        })
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

    override fun deleteByCodes(codes: List<String>): Boolean {
        if (codes.isEmpty()) return false
        var isDeleted = false
        dbManager.getConnection()?.use { conn: Connection ->
            val sql = "DELETE FROM redeem_codes WHERE code IN (${codes.joinToString { "?" }})"
            conn.prepareStatement(sql).use { statement ->
                codes.forEachIndexed { index, code ->
                    statement.setString(index + 1, code)
                }
                isDeleted = statement.executeUpdate() > 0
            }
        }
        return isDeleted
    }

    @Suppress("SqlWithoutWhere")
    override fun deleteAllCodes(): Boolean {
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

    override fun getAllCodes(): List<RedeemCode> {
        return fetchRedeemCodes("SELECT * FROM redeem_codes")
    }

    override fun getTemplateCodes(template: String, syncStatus: Boolean): List<RedeemCode> {
        if (template.isBlank()) return emptyList()
        if (syncStatus) return fetchRedeemCodes("SELECT * FROM redeem_codes WHERE ${JProperty.TEMPLATE} = ? AND ${JProperty.SYNC} = 1", template)
        return fetchRedeemCodes("SELECT * FROM redeem_codes WHERE ${JProperty.TEMPLATE} = ? ", template)
    }

    private fun fetchRedeemCodes(query: String, vararg params: Any): List<RedeemCode> {
        val codes = mutableListOf<RedeemCode>()
        dbManager.getConnection()?.use { conn ->
            conn.prepareStatement(query).use { statement ->
                params.forEachIndexed { index, param ->
                    when (param) {
                        is String -> statement.setString(index + 1, param)
                        else -> statement.setObject(index + 1, param)
                    }
                }
                val result = statement.executeQuery()
                while (result.next()) {
                    codes.add(Converter.mapResultSetToRedeemCode(result))
                }
            }
        }
        return codes
    }

}
