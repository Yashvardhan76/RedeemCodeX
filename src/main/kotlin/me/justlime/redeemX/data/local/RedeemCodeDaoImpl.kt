package me.justlime.redeemX.data.local

import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.utilities.Converter
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

class RedeemCodeDaoImpl(private val dbManager: DatabaseManager) : RedeemCodeDao {
    lateinit var getFetchCodes: List<String>
    private var getTargetList: MutableMap<String, MutableList<String>> = mutableMapOf() //<code,list of targets>
    private val converter = Converter()

    fun getTargetList(code: String): MutableList<String> {
        return getTargetList[code] ?: mutableListOf()
    }

    init {
        fetch()
    }
    fun fetch() {
        createTable()
        fetchCodes()
        fetchTargetList()
    }

    private fun fetchCodes() {
        getFetchCodes = getEntireCodes().map { it.code }
    }

    private fun fetchTargetList() {
        getEntireCodes().forEach { state ->
            getTargetList[state.code] = state.target
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
        dbManager.getConnection()?.use { conn ->
            conn.prepareStatement(
                """
                INSERT INTO redeem_codes (code, commands, storedTime, duration, isEnabled, max_redeems, max_player, 
                permission, pin, target, usedBy, template, templateLocked, storedCooldown, cooldown)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                val mappedData = converter.mapRedeemCodeToDatabase(redeemCode)

                statement.setString(1, mappedData.code)
                statement.setString(2, mappedData.commands)
                statement.setTimestamp(3, mappedData.storedTime)
                statement.setString(4, mappedData.duration)
                statement.setBoolean(5, mappedData.isEnabled)
                statement.setInt(6, mappedData.redemptionLimit)
                statement.setInt(7, mappedData.playerLimit)
                statement.setString(8, mappedData.permission)
                statement.setInt(9, mappedData.pin)
                statement.setString(10, mappedData.target)
                statement.setString(11, mappedData.usedBy)
                statement.setString(12, mappedData.template)
                statement.setBoolean(13, mappedData.templateLocked)
                statement.setTimestamp(14, mappedData.storedCooldown)
                statement.setString(15, mappedData.cooldown)

                isSuccess = statement.executeUpdate() > 0
            }
        }
        fetch()
        return isSuccess
    }

    override fun get(code: String): RedeemCode? {
        return fetchRedeemCodes("SELECT * FROM redeem_codes WHERE code = ?", code).firstOrNull()
    }

    override fun getCachedCodes(): List<String> {
        return getFetchCodes
    }

    override fun getTemplate(template: String): RedeemCode? {
        return fetchRedeemCodes("SELECT * FROM redeem_codes WHERE template = ? LIMIT 1", template).firstOrNull()
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
