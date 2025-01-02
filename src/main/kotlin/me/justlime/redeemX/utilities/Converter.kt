package me.justlime.redeemX.utilities

import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import me.justlime.redeemX.enums.JProperty
import me.justlime.redeemX.models.RedeemCode
import me.justlime.redeemX.models.RedeemCodeDatabase
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*

object Converter {
    private val gson = GsonBuilder().registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
        JsonPrimitive(src.toInstant().toString())
    }).create()

    private val listType: Type = object : TypeToken<MutableList<String>>() {}.type
    private val usedByType: Type = object : TypeToken<MutableMap<String, Int>>() {}.type
    private val lastRedeemedType: Type = object : TypeToken<MutableMap<String, Timestamp>>() {}.type

    fun mapResultSetToRedeemCode(result: ResultSet): RedeemCode {
        return RedeemCode(
            code = result.getString(JProperty.CODE.property),
            enabledStatus = result.getBoolean(JProperty.ENABLED.property),
            template = result.getString(JProperty.TEMPLATE.property),
            sync = result.getBoolean(JProperty.SYNC.property),
            duration = result.getString(JProperty.DURATION.property),
            cooldown = result.getString(JProperty.COOLDOWN.property),
            permission = result.getString(JProperty.PERMISSION.property),
            pin = result.getIntOrNull(JProperty.PIN.property) ?: 0,
            redemption = result.getInt(JProperty.REDEMPTION.property),
            playerLimit = result.getInt(JProperty.PLAYER_LIMIT.property),
            usedBy = safeFromJson(result.getString(JProperty.USED_BY.property), usedByType) ?: mutableMapOf(),
            validFrom = result.getTimestampOrNull(JProperty.VALID_FROM.property) ?: JService.getCurrentTime(),
            lastRedeemed = safeFromJson(result.getString(JProperty.LAST_REDEEMED.property), lastRedeemedType) ?: mutableMapOf(),
            target = safeFromJson(result.getString(JProperty.TARGET.property), listType) ?: mutableListOf(),
            commands = safeFromJson(result.getString(JProperty.COMMANDS.property), listType) ?: mutableListOf(),
            modified = result.getTimestamp(JProperty.MODIFIED.property),
            rewards = deserializeItemStackList(result.getString(JProperty.REWARDS.property)) ?: mutableListOf()
        )
    }

    fun mapRedeemCodeToDatabase(redeemCode: RedeemCode): RedeemCodeDatabase {
        return RedeemCodeDatabase(
            code = redeemCode.code,
            commands = gson.toJson(redeemCode.commands),
            validFrom = redeemCode.validFrom,
            duration = redeemCode.duration,
            enabled = redeemCode.enabledStatus,
            redemption = redeemCode.redemption,
            playerLimit = redeemCode.playerLimit,
            permission = redeemCode.permission,
            pin = redeemCode.pin,
            target = gson.toJson(redeemCode.target),
            usedBy = gson.toJson(redeemCode.usedBy),
            template = redeemCode.template,
            sync = redeemCode.sync,
            lastRedeemed = gson.toJson(redeemCode.lastRedeemed),
            cooldown = redeemCode.cooldown,
            rewards = serializeItemStackList(redeemCode.rewards),
            messages = redeemCode.messages,
            sound = redeemCode.sound,
            last_modified = redeemCode.modified
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

    fun serializeItemStackList(items: MutableList<ItemStack>): String {
        return items.joinToString(",") { serializeItemStack(it) }
    }

    fun deserializeItemStackList(data: String?): MutableList<ItemStack>? {
        return data?.split(",")?.mapNotNull { deserializeItemStack(it) }?.toMutableList()
    }

    private fun serializeItemStack(item: ItemStack): String {
        val outputStream = ByteArrayOutputStream()
        BukkitObjectOutputStream(outputStream).use { it.writeObject(item) }
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    private fun deserializeItemStack(data: String?): ItemStack? {
        if (data.isNullOrEmpty()) return null
        return try {
            val bytes = Base64.getDecoder().decode(data)
            val inputStream = ByteArrayInputStream(bytes)
            BukkitObjectInputStream(inputStream).use { it.readObject() as? ItemStack }
        } catch (e: Exception) {
            e.printStackTrace() // Log the error for debugging
            null
        }
    }

}
