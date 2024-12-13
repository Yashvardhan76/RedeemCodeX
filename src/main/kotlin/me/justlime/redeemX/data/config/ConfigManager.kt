package me.justlime.redeemX.data.config

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.utilities.RedeemCodeService
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.logging.Level

class ConfigManager(val plugin: RedeemX) {


    private val configFiles = mutableMapOf<JFiles, FileConfiguration>()
    private val service = RedeemCodeService()

    init {

        plugin.saveDefaultConfig()
        getConfig(JFiles.MESSAGES)
        getConfig(JFiles.CONFIG)
        getConfig(JFiles.TEMPLATE)
    }

    fun getConfig(configFile: JFiles): FileConfiguration {
        return configFiles.computeIfAbsent(configFile) {
            val file = File(plugin.dataFolder, it.filename)
            if (!file.exists()) plugin.saveResource(it.filename, false)
            YamlConfiguration.loadConfiguration(file)
        }
    }

    fun loadConfigs() {
        reloadConfig(JFiles.MESSAGES)
        reloadConfig(JFiles.CONFIG)
        reloadConfig(JFiles.TEMPLATE)
    }

    fun reloadConfig(configFile: JFiles) {
        try {
            configFiles[configFile] = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, configFile.filename))
            plugin.logger.log(Level.INFO, "${configFile.filename} reloaded successfully.")
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not reload ${configFile.filename}: ${e.message}")
        }
    }


    fun saveConfig(configFile: JFiles) {
        try {
            getConfig(configFile).save(File(plugin.dataFolder, configFile.filename))
            plugin.logger.log(Level.INFO, "${configFile.filename} saved successfully.")
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not save ${configFile.filename}: ${e.message}")
        }
    }

    fun reloadAllConfigs() {
        getConfig(JFiles.MESSAGES)
        getConfig(JFiles.CONFIG)
        getConfig(JFiles.TEMPLATE)
    }

}
