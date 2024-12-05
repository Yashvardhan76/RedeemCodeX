package me.justlime.redeemX.data.config

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.state.RedeemCodeState
import me.justlime.redeemX.utilities.RedeemCodeService
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.logging.Level

class ConfigManager(val plugin: RedeemX) {


    private val configFiles = mutableMapOf<JFiles, FileConfiguration>()
    private val templateNames = mutableListOf<String>()
    private val service = RedeemCodeService(plugin)

    companion object {
        private const val DEFAULT_FADE_IN = 10
        private const val DEFAULT_STAY = 70
        private const val DEFAULT_FADE_OUT = 10
    }

    init {
        plugin.saveDefaultConfig()
        getConfig(JFiles.MESSAGES)
        getConfig(JFiles.CONFIG)
        getConfig(JFiles.TEMPLATE)
        loadTemplates()
    }

    /**
     * Sends a message to the sender with support for placeholders and multiple message formats.
     */
    fun sendMsg(key: String, state: RedeemCodeState) {
        // Placeholder map for message customization
        val placeholders = state.toPlaceholdersMap()

        // Fetch different types of messages
        val chatMessage = getString("$key.chat", JFiles.MESSAGES) ?: getString(key, JFiles.MESSAGES)
        val chatMessageList = chatMessage?.split(",")


        val actionBarMessage = getString("$key.actionbar", JFiles.MESSAGES)
        val titleMessage = getString("$key.title.text", JFiles.MESSAGES) ?: getString("$key.title", JFiles.MESSAGES)
        val subtitleMessage = getString("$key.title.subtitle", JFiles.MESSAGES)
        val fadeIn = getString("$key.title.fade-in", JFiles.MESSAGES)?.toIntOrNull() ?: DEFAULT_FADE_IN
        val stay = getString("$key.title.stay", JFiles.MESSAGES)?.toIntOrNull() ?: DEFAULT_STAY
        val fadeOut = getString("$key.title.fade-out", JFiles.MESSAGES)?.toIntOrNull() ?: DEFAULT_FADE_OUT

        // Send action bar message
        actionBarMessage?.let {
            val filledMessage = applyPlaceholders(it, placeholders)
            if (state.sender is Player) {
                (state.sender as Player).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(filledMessage))
            }
        }

        // Send title message
        titleMessage?.let {
            val filledTitle = applyPlaceholders(it, placeholders)
            val filledSubtitle = subtitleMessage?.let { sub -> applyPlaceholders(sub, placeholders) }
            if (state.sender is Player) {
                (state.sender as Player).sendTitle(filledTitle, filledSubtitle ?: "", fadeIn, stay, fadeOut)
            }
        }

        // Send chat message
        chatMessage?.let {
            val filledChatMessage = chatMessageList?.map { applyPlaceholders(it, placeholders) }
            filledChatMessage?.forEach { state.sender.sendMessage(it) }
        }
    }

    private fun applyPlaceholders(message: String, placeholders: Map<String, String>): String {
        return placeholders.entries.fold(message) { msg, (placeholder, value) ->
            msg.replace("{$placeholder}", value)
        }
    }

    private fun loadTemplates() {
        val templateFile = File(plugin.dataFolder, "template.yml")
        if (!templateFile.exists()) return

        val config = YamlConfiguration.loadConfiguration(templateFile)
        templateNames.clear()
        templateNames.addAll(config.getKeys(false)) // Load all top-level keys (e.g., "template-1", "template-2")
    }

    fun getTemplateNames(): List<String> = templateNames

    fun getConfig(configFile: JFiles): FileConfiguration {
        return configFiles.computeIfAbsent(configFile) {
            val file = File(plugin.dataFolder, it.filename)
            if (!file.exists()) plugin.saveResource(it.filename, false)
            YamlConfiguration.loadConfiguration(file)
        }
    }


    private fun reloadConfig(configFile: JFiles) {
        try {
            configFiles[configFile] = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, configFile.filename))
            plugin.logger.log(Level.INFO, "${configFile.filename} reloaded successfully.")
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not reload ${configFile.filename}: ${e.message}")
        }
    }

    fun getString(key: String, configFile: JFiles = JFiles.CONFIG, applyColor: Boolean = true): String? {
        val fileConfig = getConfig(configFile)
        val message = fileConfig.getString(key) ?: return null
        return if (applyColor) service.applyColors(message) else message
    }

    fun reloadAllConfigs() {
        getConfig(JFiles.MESSAGES)
        getConfig(JFiles.CONFIG)
        getConfig(JFiles.TEMPLATE)
        loadTemplates()
    }

}
