package me.justlime.redeemX.data.repository

import me.justlime.redeemX.data.config.ConfigDao
import me.justlime.redeemX.data.config.JFiles
import me.justlime.redeemX.data.models.RedeemTemplate
import org.bukkit.configuration.file.FileConfiguration

class ConfigRepositoryDao(private val jConfig: ConfigDao) {

    fun getConfigValue(key: String): String{
        return jConfig.getString(key, JFiles.CONFIG, applyColor = false) ?: ""
    }

    fun getMessage(message: String): String {
        return jConfig.getMessage(message)
    }

    fun getFormattedMessage(message: String, placeholders: Map<String, String>): String {
        return jConfig.getFormattedMessage(message, placeholders)
    }

    fun getTemplate(template: String): RedeemTemplate {
        return jConfig.getTemplate(template)
    }

    fun getTemplateValue(template: String,property: String): String{
        return jConfig.getString("$template.$property",JFiles.TEMPLATE,false) ?: ""
    }

    fun getEntireTemplates(): List<RedeemTemplate> {
        return jConfig.getEntireTemplates()
    }

    fun modifyTemplate(template: RedeemTemplate){
        jConfig.upsertTemplate(template = template)
    }

    fun deleteTemplate(name: String): Boolean {
        return jConfig.deleteTemplate(name)
    }

    fun deleteEntireTemplates(): Boolean {
        return jConfig.deleteEntireTemplates()
    }

    fun getConfig(config: JFiles): FileConfiguration {
        return jConfig.getConfig(JFiles.CONFIG)
    }

    fun reloadConfig(): Boolean {
        return jConfig.reloadAllConfigs()
    }

}
