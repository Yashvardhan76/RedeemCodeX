package me.justlime.redeemX.data.config

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.api.RedeemXAPI.placeHolder
import me.justlime.redeemX.enums.JFiles
import me.justlime.redeemX.enums.JMessage
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemTemplate
import me.justlime.redeemX.utilities.Converter
import me.justlime.redeemX.utilities.JService
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import java.io.File
import java.util.logging.Level

class ConfigImpl(private val plugin: RedeemX) : ConfigDao {
    private val configManager = plugin.configManager

    companion object {
        private const val DEFAULT_FADE_IN = 10
        private const val DEFAULT_STAY = 70
        private const val DEFAULT_FADE_OUT = 10
    }

    override fun getString(path: String, configFile: JFiles, applyColor: Boolean): String? {
        val fileConfig = configManager.getConfig(configFile)
        val message = fileConfig.getString(path) ?: return null
        return if (applyColor) JService.applyColors(message) else message
    }

    override fun getMessage(key: String): String {
        //I have used formatted Message cause of redundant.
        val placeholders = CodePlaceHolder(plugin.server.consoleSender)
        var message = getFormattedMessage("$key.chat", placeholders).removeSurrounding("[", "]")
        if (message.isEmpty()) message = getFormattedMessage(key, placeholders).removeSurrounding("[", "]")
        if (message.isEmpty()) message = getFormattedTemplateMessage(key, placeholders).removeSurrounding("[", "]")
        if (message.isEmpty()) message = getFormattedTemplateMessage(key, placeholders).removeSurrounding("[", "]")
        if (message.isEmpty()) return ""
        return JService.removeColors(message)
    }

    override fun getMessage(key: String, placeholders: CodePlaceHolder): String {
        //I have used formatted Message cause of redundant.
        var message = getFormattedMessage("$key.chat", placeholders).removeSurrounding("[", "]")
        if (message.isEmpty()) message = getFormattedMessage(key, placeholders).removeSurrounding("[", "]")
        if (message.isEmpty()) message = getFormattedTemplateMessage(key, placeholders).removeSurrounding("[", "]")
        if (message.isEmpty()) message = getFormattedTemplateMessage(key, placeholders).removeSurrounding("[", "]")
        if (message.isEmpty()) return ""
        return JService.removeColors(message)
    }

    override fun getTemplateMessage(template: String): String {
        return getString(template, JFiles.TEMPLATE, true) ?: ""
    }

    override fun getTemplateMessage(template: String, message: String): String {
        return getString("$template.$message", JFiles.TEMPLATE, true) ?: ""
    }

    override fun getFormattedMessage(message: String, placeholders: CodePlaceHolder): String {
        return JService.applyPlaceholders(getString(message, JFiles.MESSAGES, true) ?: return "", placeholders) {
            plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")
        }.replace("{prefix}", getString(JMessage.PREFIX, JFiles.MESSAGES, true) ?: "")
    }

    override fun getFormattedTemplateMessage(message: String, placeholders: CodePlaceHolder): String {
        return JService.applyPlaceholders(getString(message, JFiles.TEMPLATE, true) ?: return "", placeholders) {
            plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")
        }
    }

    override fun sendMsg(key: String, placeHolder: CodePlaceHolder) {

        // Fetch different types of messages
        var message = getFormattedMessage("$key.chat", placeHolder).removeSurrounding("[", "]")
        if (message.isEmpty()) message = getFormattedMessage(key, placeHolder).removeSurrounding("[", "]")
        if (message.isEmpty()) message = getFormattedTemplateMessage(key, placeHolder).removeSurrounding("[", "]")
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
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(it.removePrefix(" ")))
            }
        }

        // Send title message
        titleMessage.let {
            if (placeHolder.sender is Player && it.isNotEmpty()) {
                val player = placeHolder.sender as Player
                player.sendTitle(it.removePrefix(" "), subtitleMessage, fadeIn, stay, fadeOut)
            }
        }

        // Send chat message
        chatMessage.let {
            if (chatMessage.isNotEmpty()) {
                chatMessage.forEach { placeHolder.sender.sendMessage(it.removePrefix(" ")) }
            }
        }
    }

    override fun sendTemplateMsg(template: String, placeHolder: CodePlaceHolder) {
        sendMsg("$template.messages", placeHolder)
    }

    override fun loadDefaultTemplateValues(template: String): RedeemTemplate {
        return RedeemTemplate(
            name = template,
            defaultEnabledStatus = true,
            commands = mutableListOf(),
            duration = "0s",
            cooldown = "0s",
            redemption = 1,
            playerLimit = 1,
            defaultSync = false,
            permissionRequired = false,
            permissionValue = "redeemx.use.${template.lowercase()}.{code}",
            message = mutableListOf(),
            sound = "",
            rewards = mutableListOf(),
            target = mutableListOf(),

            syncEnabledStatus = false,
            syncLockedStatus = false,
            syncTarget = false,
            syncCommands = true,
            syncDuration = true,
            syncCooldown = true,
            syncPin = true,
            syncRedemption = true,
            syncPlayerLimit = true,
            syncPermission = true,
            syncMessages = true,
            syncSound = true,
            syncRewards = true,
        )
    }

    override fun getTemplate(template: String): RedeemTemplate {
        val config = configManager.getConfig(JFiles.TEMPLATE)
        val templateSection = config.getConfigurationSection(template) ?: throw Exception(
            getMessage(
                JMessage.Template.NOT_FOUND, CodePlaceHolder(plugin.server.consoleSender)
            )
        )
        return RedeemTemplate(
            name = template,

            defaultEnabledStatus = templateSection.getBoolean("enabled", true),
            commands = templateSection.getStringList("commands"),
            duration = templateSection.getString("duration", "0s") ?: "0s",
            redemption = templateSection.getInt("redemption", 1),
            playerLimit = templateSection.getInt("player-limit", 1),
            permissionRequired = templateSection.getBoolean("permission.required", false),
            permissionValue = templateSection.getString("permission.value", "redeemx.use.${template}.{code}") ?: "redeemx.use.${template}.{code}",
            pin = templateSection.getInt("pin", 0),
            defaultSync = templateSection.getBoolean("default-sync", false),
            cooldown = templateSection.getString("cooldown", "0s") ?: "0s",
            message = templateSection.getStringList("messages"),
            sound = templateSection.getString("sound") ?: "",
            target = templateSection.getStringList("target"),
            rewards = templateSection.getString("rewards").let { Converter.deserializeItemStackList(it) } ?: mutableListOf(),

            syncEnabledStatus = templateSection.getBoolean("sync.enabled-status", false),
            syncLockedStatus = templateSection.getBoolean("sync.locked-status", false),
            syncTarget = templateSection.getBoolean("sync.target", false),

            syncCommands = templateSection.getBoolean("sync.commands", true),
            syncDuration = templateSection.getBoolean("sync.duration", true),
            syncCooldown = templateSection.getBoolean("sync.cooldown", true),
            syncPin = templateSection.getBoolean("sync.pin", true),
            syncRedemption = templateSection.getBoolean("sync.redemption", true),
            syncPlayerLimit = templateSection.getBoolean("sync.player-limit", true),
            syncPermission = templateSection.getBoolean("sync.permission", true),
            syncMessages = templateSection.getBoolean("sync.messages", true),
            syncSound = templateSection.getBoolean("sync.sound", true),
            syncRewards = templateSection.getBoolean("sync.rewards", true))
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
            section?.set("enabled", template.defaultEnabledStatus)
            section?.set("duration", template.duration)
            section?.set("cooldown", template.cooldown)
            section?.set("redemption", template.redemption)
            section?.set("player-limit", template.playerLimit)
            section?.set("permission.required", template.permissionRequired)
            section?.set("permission.value", template.permissionValue)
            section?.set("pin", template.pin)
            section?.set("default-sync", template.defaultSync)
            section?.set("messages", template.message)
            section?.set("commands", template.commands)
            section?.set("sound", template.sound)
            section?.set("rewards", Converter.serializeItemStackList(template.rewards))

            section?.set("sync.enabled-status", template.syncEnabledStatus)
            section?.set("sync.locked-status", template.syncLockedStatus)
            section?.set("sync.target", template.syncTarget)
            section?.set("sync.commands", template.syncCommands)
            section?.set("sync.duration", template.syncDuration)
            section?.set("sync.cooldown", template.syncCooldown)
            section?.set("sync.pin", template.syncPin)
            section?.set("sync.redemption", template.syncRedemption)
            section?.set("sync.player-limit", template.syncPlayerLimit)
            section?.set("sync.permission", template.syncPermission)
            section?.set("sync.messages", template.syncMessages)
            section?.set("sync.sound", template.syncSound)
            section?.set("sync.rewards", template.syncRewards)
            config.save(File(plugin.dataFolder, JFiles.TEMPLATE.filename))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun deleteTemplate(name: String): Boolean {
        if (name == "DEFAULT") return false
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
            config.getKeys(false).filter { it != "DEFAULT" }.forEach { config.set(it, null) }
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
            configManager.getConfig(configFile)
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