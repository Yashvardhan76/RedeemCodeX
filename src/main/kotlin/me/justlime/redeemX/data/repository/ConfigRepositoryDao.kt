package me.justlime.redeemX.data.repository

import me.justlime.redeemX.data.config.ConfigDao
import me.justlime.redeemX.data.config.JFiles
import me.justlime.redeemX.data.models.RedeemTemplate
import org.bukkit.configuration.file.FileConfiguration

class ConfigRepositoryDao(private val jConfig: ConfigDao) : ConfigRepositoryHandler {
    override fun getMessage(message: String): String {
        return jConfig.getMessage(message)
    }

    override fun getFormattedMessage(message: String, placeholders: Map<String, String>): String {
        return jConfig.getFormattedMessage(message, placeholders)
    }

    override fun getTemplate(template: String): RedeemTemplate {
        return jConfig.getTemplate(template)
    }

    override fun getEntireTemplates(): List<RedeemTemplate> {
        return jConfig.getEntireTemplates()
    }

    override fun upsertTemplate(template: RedeemTemplate): Boolean {
        return jConfig.upsertTemplate(template)
    }

    override fun deleteTemplate(name: String): Boolean {
        return jConfig.deleteTemplate(name)
    }

    override fun deleteEntireTemplates(): Boolean {
        return jConfig.deleteEntireTemplates()
    }

    override fun getConfig(config: JFiles): FileConfiguration {
        return jConfig.getConfig(JFiles.CONFIG)
    }

    override fun reloadConfig(): Boolean {
        return jConfig.reloadAllConfigs()
    }

}
