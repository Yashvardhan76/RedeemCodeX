package me.justlime.redeemX.config

import me.justlime.redeemX.RedeemX
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager(private val plugin: RedeemX) {
    private val messageFileName = "messages.yml"
    private var messageFile: File = File(plugin.dataFolder,messageFileName)
    var message: FileConfiguration = YamlConfiguration.loadConfiguration(messageFile)
    var config: FileConfiguration = plugin.config

    init{
        plugin.saveDefaultConfig()
        if(!messageFile.exists()) plugin.saveResource(messageFileName,false)
    }

    fun saveConfig(){
        try {
            plugin.config.save(messageFile)

        } catch (e: Exception){
            plugin.logger.info("Could not save $messageFileName")
        }
        plugin.saveDefaultConfig()
    }

    fun reloadConfig(){
        try {
            message = YamlConfiguration.loadConfiguration(messageFile)
            plugin.logger.info("$messageFileName reloaded successfully.")
        } catch (e: Exception) {
            plugin.logger.severe("Could not reload $messageFileName: ${e.message}")
        }
        plugin.reloadConfig()
    }


}