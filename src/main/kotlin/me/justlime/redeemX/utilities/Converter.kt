package me.justlime.redeemX.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
            code = result.getString("code"),
            commands = gson.fromJson(result.getString("commands"), object : TypeToken<MutableMap<Int, String>>() {}.type),
            storedTime = result.getTimestampOrNull("storedTime") ?: RedeemCodeService().currentTime,
            duration = result.getString("duration"),
            isEnabled = result.getBoolean("isEnabled"),
            maxRedeems = result.getInt("max_redeems"),
            maxPlayers = result.getInt("max_player"),
            permission = result.getString("permission"),
            pin = result.getIntOrNull("pin")?: 0,
            target = gson.fromJson(result.getString("target"), object : TypeToken<MutableList<String>>() {}.type),
            usage = gson.fromJson(result.getString("usedBy"), object : TypeToken<MutableMap<String, Int>>() {}.type),
            template = result.getString("template"),
            templateLocked = result.getBoolean("templateLocked"),
            storedCooldown = result.getTimestampOrNull("storedCooldown") ?: RedeemCodeService().currentTime,
            cooldown = result.getString("cooldown")
        )
    }
    // Maps a RedeemCode to a RedeemCodeDatabase object for storage in the database
    fun mapRedeemCodeToDatabase(redeemCode: RedeemCode): RedeemCodeDatabase {
        return RedeemCodeDatabase(
            code = redeemCode.code,
            commands = gson.toJson(redeemCode.commands),
            storedTime = redeemCode.storedTime,
            duration = redeemCode.duration,
            isEnabled = redeemCode.isEnabled,
            redemptionLimit = redeemCode.maxRedeems,
            playerLimit = redeemCode.maxPlayers,
            permission = redeemCode.permission,
            pin = redeemCode.pin,
            target = gson.toJson(redeemCode.target),
            usedBy = gson.toJson(redeemCode.usage),
            template = redeemCode.template,
            templateLocked = redeemCode.templateLocked,
            storedCooldown = redeemCode.storedCooldown,
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
