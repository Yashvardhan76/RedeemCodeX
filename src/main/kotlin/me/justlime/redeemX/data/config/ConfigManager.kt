package me.justlime.redeemX.data.config

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.enums.JFiles
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.logging.Level

class ConfigManager(val plugin: RedeemX) {

    private val configFiles = mutableMapOf<JFiles, FileConfiguration>()

    init {

        plugin.saveDefaultConfig()
        getConfig(JFiles.MESSAGES)
        getConfig(JFiles.CONFIG)
        getConfig(JFiles.TEMPLATE)
    }

    fun getConfig(configFile: JFiles): FileConfiguration {
        val file = File(plugin.dataFolder, configFile.filename)
        if (!file.exists()) plugin.saveResource(configFile.filename, false)
        return YamlConfiguration.loadConfiguration(file)

    }

    fun saveConfig(configFile: JFiles) {
        try {
            getConfig(configFile).save(File(plugin.dataFolder, configFile.filename))
            plugin.logger.log(Level.INFO, "${configFile.filename} saved successfully.")
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not save ${configFile.filename}: ${e.message}")
        }
    }
}
