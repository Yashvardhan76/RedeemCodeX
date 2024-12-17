package me.justlime.redeemX.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.justlime.redeemX.enums.JProperty
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.models.RedeemCodeDatabase
import java.sql.ResultSet
import java.sql.Timestamp

class Converter {
    private val gson = Gson()

    // Converts RedeemCode to a database string (JSON)
    fun toDatabaseString(redeemCode: RedeemCode): String {
        return gson.toJson(redeemCode)
    }

    // Converts the database string back to a RedeemCode object
    fun fromDatabaseString(databaseString: String): RedeemCode {
        return gson.fromJson(databaseString, RedeemCode::class.java)
    }

    // Maps a ResultSet row to a RedeemCode
    fun mapResultSetToRedeemCode(result: ResultSet): RedeemCode {
        return RedeemCode(
            code = result.getString(JProperty.CODE.property),
            commands = gson.fromJson(result.getString(JProperty.COMMANDS.property), object : TypeToken<MutableMap<Int, String>>() {}.type),
            validFrom = result.getTimestampOrNull(JProperty.VALID_FROM.property) ?: RedeemCodeService().currentTime,
            duration = result.getString(JProperty.DURATION.property),
            enabled = result.getBoolean(JProperty.ENABLED.property),
            redemption = result.getInt(JProperty.REDEMPTION.property),
            limit = result.getInt(JProperty.LIMIT.property),
            permission = result.getString(JProperty.PERMISSION.property),
            pin = result.getIntOrNull(JProperty.PIN.property)?: 0,
            target = gson.fromJson(result.getString(JProperty.TARGET.property), object : TypeToken<MutableList<String>>() {}.type),
            usedBy = gson.fromJson(result.getString(JProperty.USED_BY.property), object : TypeToken<MutableMap<String, Int>>() {}.type),
            template = result.getString(JProperty.TEMPLATE.property),
            locked = result.getBoolean(JProperty.LOCKED.property),
            lastRedeemed = gson.fromJson(result.getString(JProperty.LAST_REDEEMED.property), object : TypeToken<MutableMap<String, Timestamp>>() {}.type),
            cooldown = result.getString(JProperty.COOLDOWN.property)
        )
    }
    // Maps a RedeemCode to a RedeemCodeDatabase object for storage in the database
    fun mapRedeemCodeToDatabase(redeemCode: RedeemCode): RedeemCodeDatabase {
        return RedeemCodeDatabase(
            code = redeemCode.code,
            commands = gson.toJson(redeemCode.commands),
            validFrom = redeemCode.validFrom,
            duration = redeemCode.duration,
            isEnabled = redeemCode.enabled,
            redemption = redeemCode.redemption,
            limit = redeemCode.limit,
            permission = redeemCode.permission,
            pin = redeemCode.pin,
            target = gson.toJson(redeemCode.target),
            usedBy = gson.toJson(redeemCode.usedBy),
            template = redeemCode.template,
            templateLocked = redeemCode.locked,
            lastRedeemed = gson.toJson(redeemCode.lastRedeemed),
            cooldown = redeemCode.cooldown
        )
    }


    // Nullable integer utility
    private fun ResultSet.getIntOrNull(columnLabel: String): Int? {
        val value = this.getInt(columnLabel)
        return if (this.wasNull()) null else value
    }

    // Nullable timestamp utility
    private fun ResultSet.getTimestampOrNull(columnLabel: String): Timestamp? {
        val value = this.getTimestamp(columnLabel)
        return if (this.wasNull()) null else value
    }
}
