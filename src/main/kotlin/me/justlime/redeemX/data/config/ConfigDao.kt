package me.justlime.redeemX.data.config

import org.bukkit.configuration.file.FileConfiguration

interface ConfigDao {
    fun getString(path: String, configFile: JFiles, applyColor: Boolean): String?
    fun getMessage(message: String): String
    fun getFormattedMessage(message: String, placeholders: Map<String, String>): String

    fun getTemplate(template: String): me.justlime.redeemX.data.models.RedeemTemplate
    fun getEntireTemplates(): List<me.justlime.redeemX.data.models.RedeemTemplate>
    fun upsertTemplate(template: me.justlime.redeemX.data.models.RedeemTemplate): Boolean
    fun deleteTemplate(name: String): Boolean
    fun deleteEntireTemplates(): Boolean

    fun getConfig(configFile: JFiles): FileConfiguration
    fun upsertConfig(configFile: JFiles, path: String,value: String): Boolean
    fun saveAllConfigs(): Boolean
    fun reloadConfig(configFile: JFiles): Boolean
    fun reloadAllConfigs(): Boolean
}
