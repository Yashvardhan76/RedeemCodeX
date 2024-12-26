package me.justlime.redeemX.data.config

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.enums.JFiles
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemTemplate
import me.justlime.redeemX.utilities.RedeemCodeService
import me.justlime.redeemX.utilities.toIndexedMap
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.logging.Level

class ConfigImpl(private val plugin: RedeemX) : ConfigDao {
    private val configFiles = mutableMapOf<JFiles, FileConfiguration>()
    private val configManager = plugin.configManager
    private val service = RedeemCodeService()

    companion object {
        private const val DEFAULT_FADE_IN = 10
        private const val DEFAULT_STAY = 70
        private const val DEFAULT_FADE_OUT = 10
    }

    override fun getString(path: String, configFile: JFiles, applyColor: Boolean): String? {
        val fileConfig = configManager.getConfig(configFile)
        val message = fileConfig.getString(path) ?: return null
        return if (applyColor) service.applyColors(message) else message
    }

    //TODO Implement the method which remove the 'HEX' color code from the message
    override fun getMessage(message: String): String {
        //I have used formatted Message cause of redundant.
        val placeholders = CodePlaceHolder(plugin.server.consoleSender)
        return service.removeColors(getFormattedMessage(message, placeholders))
    }

    override fun getMessage(message: String, placeholders: CodePlaceHolder): String {
        //I have used formatted Message cause of redundant.
        return service.removeColors(getFormattedMessage(message, placeholders))
    }

    override fun getTemplateMessage(template: String): String {
        return getString(template, JFiles.TEMPLATE, true) ?: ""
    }

    override fun getTemplateMessage(template: String, message: String): String {
        return getString("$template.$message", JFiles.TEMPLATE, true) ?: ""
    }

    override fun getFormattedMessage(message: String, placeholders: CodePlaceHolder): String {
        return service.applyPlaceholders(getString(message, JFiles.MESSAGES, true) ?: return "", placeholders){
            plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")
        }
    }
    override fun getFormattedTemplateMessage(message: String, placeholders: CodePlaceHolder): String {
        return service.applyPlaceholders(getString(message, JFiles.TEMPLATE, true) ?: return "", placeholders){
            plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")
        }
    }


    override fun sendMsg(key: String, placeHolder: CodePlaceHolder) {

        // Fetch different types of messages
        var message = getFormattedMessage("$key.chat", placeHolder).removeSurrounding("[", "]")
        if (message.isEmpty()) message = getFormattedMessage(key, placeHolder).removeSurrounding("[", "]")
        if(message.isEmpty()) message = getFormattedTemplateMessage(key, placeHolder).removeSurrounding("[", "]")
        if (message.isEmpty()) message = getFormattedTemplateMessage(key, placeHolder).removeSurrounding("[", "]")
        if (message.isEmpty()) return

        val chatMessage: MutableList<String> = message.split(",").toMutableList()
        val actionBarMessage = getFormattedMessage("$key.actionbar", placeHolder)
        val titleMessage = getFormattedMessage("$key.title.main", placeHolder)
        val subtitleMessage = getFormattedMessage("$key.title.sub", placeHolder)
        val fadeIn = getFormattedMessage("$key.title.fadeIn", placeHolder).toIntOrNull() ?: DEFAULT_FADE_IN
        val stay = getFormattedMessage("$key.title.stay", placeHolder).toIntOrNull() ?: DEFAULT_STAY
        val fadeOut = getFormattedMessage("$key.title.fadeOut", placeHolder).toIntOrNull() ?: DEFAULT_FADE_OUT

        // Send action bar message
        actionBarMessage.let {
            if (placeHolder.sender is Player && actionBarMessage.isNotEmpty()) {
                val player = placeHolder.sender as Player
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(it))
            }
        }

        // Send title message
        titleMessage.let {
            if (placeHolder.sender is Player && it.isNotEmpty()) {
                val player = placeHolder.sender as Player
                player.sendTitle(it, subtitleMessage, fadeIn, stay, fadeOut)
            }
        }

        // Send chat message
        chatMessage.let {
            if (chatMessage.isNotEmpty()) {
                chatMessage.forEach { placeHolder.sender.sendMessage(it.trim()) }
            }
        }
    }

    override fun sendTemplateMsg(template: String, placeHolder: CodePlaceHolder) {
        sendMsg("$template.messages",placeHolder)
    }

    override fun getTemplate(template: String): RedeemTemplate {
        val config = configManager.getConfig(JFiles.TEMPLATE)
        val templateSection = config.getConfigurationSection(template) ?: throw Exception(getMessage(JMessage.Commands.ModifyTemplate.NOT_FOUND,CodePlaceHolder(plugin.server.consoleSender)))
        return RedeemTemplate(
            name = template,
            commands = templateSection.getStringList("commands").toIndexedMap(),
            duration = templateSection.getString("duration") ?: "0s",
            redemption = templateSection.getInt("redemption", 1),
            playerLimit = templateSection.getInt("limit", 1),
            permissionRequired = templateSection.getBoolean("permission.required", false),
            permissionValue = templateSection.getString("permission.value","") ?: "",
            pin = templateSection.getInt("pin", 0),
            locked = templateSection.getBoolean("locked", false),
            cooldown = templateSection.getString("cooldown") ?: "0s",
            message = templateSection.getString("messages")?.split(",")?.toMutableList() ?: mutableListOf()
        )
    }

    override fun getEntireTemplates(): List<RedeemTemplate> {
        val config = configManager.getConfig(JFiles.TEMPLATE)
        return config.getKeys(false).map { getTemplate(it) }
    }

    override fun upsertTemplate(template: RedeemTemplate): Boolean {

        try {
            val config = configManager.getConfig(JFiles.TEMPLATE)
            config.createSection(template.name)
            val section = config.getConfigurationSection(template.name)
            section?.set("commands", template.commands)
            section?.set("duration", template.duration)
            section?.set("redemption", template.redemption)
            section?.set("limit", template.playerLimit)
            section?.set("permission.required", template.permissionRequired)
            section?.set("permission.value", template.permissionValue)
            section?.set("pin", template.pin)
            section?.set("locked", template.locked)
            section?.set("cooldown", template.cooldown)
            section?.set("messages", template.message)
            config.save(File(plugin.dataFolder, JFiles.TEMPLATE.filename))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun deleteTemplate(name: String): Boolean {
        if (name == "default") return false
        try {
            val config = configManager.getConfig(JFiles.TEMPLATE)
            config.set(name, null)
            config.save(File(plugin.dataFolder, JFiles.TEMPLATE.filename))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun deleteAllTemplates(): Boolean {
        val config = configManager.getConfig(JFiles.TEMPLATE)
        if (config.getKeys(false).isEmpty()) return true
        try {
            config.getKeys(false).forEach { config.set(it, null) }
            config.options().copyDefaults(true)
            config.save(File(plugin.dataFolder, JFiles.TEMPLATE.filename))
            return true
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not delete templates: ${e.message}")
            return false
        }
    }

    override fun upsertConfig(configFile: JFiles, path: String, value: String): Boolean {
        try {
            val config = configManager.getConfig(configFile)
            config.set(path, value)
            config.save(File(plugin.dataFolder, configFile.filename))
            return true
        } catch (e: Exception) {
            return false
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
        try {
            JFiles.entries.forEach { configManager.saveConfig(it) }
            return true
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not save configs: ${e.message}")
            return false
        }
    }

    override fun reloadAllConfigs(): Boolean {
        return JFiles.entries.forEach { reloadConfig(it) } == Unit
    }

}