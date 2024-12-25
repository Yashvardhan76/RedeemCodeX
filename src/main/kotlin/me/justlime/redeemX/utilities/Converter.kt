package me.justlime.redeemX.utilities

import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import me.justlime.redeemX.enums.JProperty
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.models.RedeemCodeDatabase
import java.lang.reflect.Type
import java.sql.ResultSet
import java.sql.Timestamp

class Converter {
    private val gson = GsonBuilder().registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
        JsonPrimitive(src.toInstant().toString())
    }).create()

    companion object {
        val commandsType: Type = object : TypeToken<MutableMap<Int, String>>() {}.type
        val targetType: Type = object : TypeToken<MutableList<String>>() {}.type
        val usedByType: Type = object : TypeToken<MutableMap<String, Int>>() {}.type
        val lastRedeemedType: Type = object : TypeToken<MutableMap<String, Timestamp>>() {}.type
    }

    fun mapResultSetToRedeemCode(result: ResultSet): RedeemCode {
        return RedeemCode(
            code = result.getString(JProperty.CODE.property),
            commands = safeFromJson(result.getString(JProperty.COMMANDS.property), commandsType) ?: mutableMapOf(),
            validFrom = result.getTimestampOrNull(JProperty.VALID_FROM.property) ?: RedeemCodeService().getCurrentTime(),
            duration = result.getString(JProperty.DURATION.property),
            enabled = result.getBoolean(JProperty.ENABLED.property),
            redemption = result.getInt(JProperty.REDEMPTION.property),
            playerLimit = result.getInt(JProperty.PLAYER_LIMIT.property),
            permission = result.getString(JProperty.PERMISSION.property),
            pin = result.getIntOrNull(JProperty.PIN.property) ?: 0,
            target = safeFromJson(result.getString(JProperty.TARGET.property), targetType) ?: mutableListOf(),
            usedBy = safeFromJson(result.getString(JProperty.USED_BY.property), usedByType) ?: mutableMapOf(),
            template = result.getString(JProperty.TEMPLATE.property),
            locked = result.getBoolean(JProperty.LOCKED.property),
            lastRedeemed = safeFromJson(result.getString(JProperty.LAST_REDEEMED.property), lastRedeemedType) ?: mutableMapOf(),
            cooldown = result.getString(JProperty.COOLDOWN.property),
            modified = result.getTimestamp(JProperty.MODIFIED.property)
        )
    }

    fun mapRedeemCodeToDatabase(redeemCode: RedeemCode): RedeemCodeDatabase {
        return RedeemCodeDatabase(
            code = redeemCode.code,
            commands = gson.toJson(redeemCode.commands),
            validFrom = redeemCode.validFrom,
            duration = redeemCode.duration,
            enabled = redeemCode.enabled,
            redemption = redeemCode.redemption,
            playerLimit = redeemCode.playerLimit,
            permission = redeemCode.permission,
            pin = redeemCode.pin,
            target = gson.toJson(redeemCode.target),
            usedBy = gson.toJson(redeemCode.usedBy),
            template = redeemCode.template,
            locked = redeemCode.locked,
            lastRedeemed = gson.toJson(redeemCode.lastRedeemed),
            cooldown = redeemCode.cooldown
        )
    }

    private fun <T> safeFromJson(json: String?, type: Type): T? {
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    private fun ResultSet.getIntOrNull(columnLabel: String): Int? {
        val value = this.getInt(columnLabel)
        return if (this.wasNull()) null else value
    }

    private fun ResultSet.getTimestampOrNull(columnLabel: String): Timestamp? {
        val value = this.getTimestamp(columnLabel)
        return if (this.wasNull()) null else value
    }
}
