package me.justlime.redeemX.data.repository

import me.justlime.redeemX.data.config.JFiles
import me.justlime.redeemX.data.models.RedeemTemplate
import org.bukkit.configuration.file.FileConfiguration

interface ConfigRepositoryHandler {
    fun getMessage(message: String): String

    fun getFormattedMessage(message: String, placeholders: Map<String, String>): String

    fun getTemplate(template: String): RedeemTemplate

    fun getEntireTemplates(): List<RedeemTemplate>

    fun upsertTemplate(template: RedeemTemplate): Boolean

    //Delete Template By name
    fun deleteTemplate(name: String): Boolean

    fun deleteEntireTemplates(): Boolean

    fun getConfig(config: JFiles): FileConfiguration

    fun reloadConfig(): Boolean

}