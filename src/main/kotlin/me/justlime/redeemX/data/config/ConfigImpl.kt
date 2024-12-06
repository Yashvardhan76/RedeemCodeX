package me.justlime.redeemX.data.config

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.models.RedeemTemplate
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.logging.Level

class ConfigImpl(private val plugin: RedeemX, private val configManager: ConfigManager): ConfigDao {
    private val configFiles = mutableMapOf<JFiles, FileConfiguration>()
    private val templateNames = mutableListOf<String>()
    private val service = RedeemCodeService(plugin)


    companion object {
        private const val DEFAULT_FADE_IN = 10
        private const val DEFAULT_STAY = 70
        private const val DEFAULT_FADE_OUT = 10
    }

    override fun getString(path: String, configFile: JFiles, applyColor: Boolean): String? {
        val fileConfig = getConfig(configFile)
        val message = fileConfig.getString(path) ?: return null
        return if (applyColor) service.applyColors(message) else message
    }

    override fun getMessage(message: String): String {
        return getString(path = message,configFile=JFiles.MESSAGES,applyColor = false) ?: return ""
    }

    override fun getFormattedMessage(message: String, placeholders: Map<String, String>): String {
        return configManager.getString(key = message,configFile=JFiles.MESSAGES,applyColor = true) ?: return ""
    }

    override fun getTemplate(template: String): RedeemTemplate {
        val config = configManager.getConfig(JFiles.TEMPLATE)
        val templateSection = config.getConfigurationSection(template) ?: throw Exception("Template not found")
        return RedeemTemplate(
            name = template,
            commands = templateSection.getString("commands") ?: "",
            duration = templateSection.getString("duration") ?: "",
            isEnabled = templateSection.getBoolean("isEnabled", true),
            maxRedeems = templateSection.getInt("maxRedeems", 1),
            maxPlayers = templateSection.getInt("maxPlayers", 1),
            permissionRequired = templateSection.getBoolean("permissionRequired", false),
            permissionValue = templateSection.getString("permissionValue") ?: "",
            pin = templateSection.getInt("pin", 0),
            codeGenerateDigit = templateSection.getInt("codeGenerateDigit", 4),
            codeExpiredDuration = templateSection.getString("codeExpiredDuration") ?: ""
        )
    }

    override fun getEntireTemplates(): List<RedeemTemplate> {
        val config = getConfig(JFiles.TEMPLATE)
        return config.getKeys(false).map { getTemplate(it) }
    }

    override fun upsertTemplate(template: RedeemTemplate): Boolean {

        try {
            val config = getConfig(JFiles.TEMPLATE)
            config.createSection(template.name)
            val section = config.getConfigurationSection(template.name)
            section?.set("commands", template.commands)
            section?.set("duration", template.duration)
            section?.set("isEnabled", template.isEnabled)
            section?.set("maxRedeems", template.maxRedeems)
            section?.set("maxPlayers", template.maxPlayers)
            section?.set("permissionRequired", template.permissionRequired)
            section?.set("permissionValue", template.permissionValue)
            section?.set("pin", template.pin)
            section?.set("codeGenerateDigit", template.codeGenerateDigit)
            section?.set("codeExpiredDuration", template.codeExpiredDuration)
            config.save(File(plugin.dataFolder, JFiles.TEMPLATE.filename))
            return true
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not modify template: ${e.message}")
            return false
        }
    }

    override fun deleteTemplate(name: String): Boolean {
        try {
            val config = getConfig(JFiles.TEMPLATE)
            config.set(name, null)
            config.save(File(plugin.dataFolder, JFiles.TEMPLATE.filename))
            return true
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not delete template: ${e.message}")
            return false
        }
    }

    override fun deleteEntireTemplates(): Boolean {
        if((getConfig(JFiles.TEMPLATE).getKeys(false).isEmpty())) return true

        try {
            val config = getConfig(JFiles.TEMPLATE)
            config.getKeys(false).forEach { config.set(it, null) }
            config.save(File(plugin.dataFolder, JFiles.TEMPLATE.filename))
            return true
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not delete templates: ${e.message}")
            return false
        }

    }

    override fun getConfig(configFile: JFiles): FileConfiguration {
        return configFiles.computeIfAbsent(configFile) {
            val file = File(plugin.dataFolder, it.filename)
            if (!file.exists()) plugin.saveResource(it.filename, false)
            YamlConfiguration.loadConfiguration(file)
        }
    }

    override fun upsertConfig(configFile: JFiles, content: String): Boolean {
        return try {
            val file = File(plugin.dataFolder, configFile.filename)
            val config = YamlConfiguration.loadConfiguration(file)
            config.loadFromString(content)
            config.save(file)
            true
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not upsert config: ${e.message}")
            false
        }
    }

    override fun reloadConfig(configFile: JFiles): Boolean {
        try {
            configFiles[configFile] = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, configFile.filename))
            return true
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not reload ${configFile.filename}: ${e.message}")
            return false
        }
    }

    override fun saveAllConfigs(): Boolean {
        JFiles.entries.forEach { configFile ->
            try {
                val file = File(plugin.dataFolder, configFile.filename)
                getConfig(configFile).save(file)
                plugin.logger.log(Level.INFO, "${configFile.filename} saved successfully.")
                return true
            } catch (e: Exception) {
                plugin.logger.log(Level.SEVERE, "Could not save ${configFile.filename}: ${e.message}")
                return false
            }
        }
        return false
    }

    override fun reloadAllConfigs(): Boolean {
       return JFiles.entries.forEach{ reloadConfig(it)} == Unit
    }



}