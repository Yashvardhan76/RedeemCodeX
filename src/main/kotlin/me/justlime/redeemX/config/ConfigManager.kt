package me.justlime.redeemX.config

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.state.RedeemCodeState
import me.justlime.redeemX.state.StateManager
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.logging.Level
import java.util.regex.Pattern

class ConfigManager(private val plugin: RedeemX, private val stateManager: StateManager) {

    private val configFiles = mutableMapOf<Files, FileConfiguration>()

    companion object {
        private const val DEFAULT_FADE_IN = 10
        private const val DEFAULT_STAY = 70
        private const val DEFAULT_FADE_OUT = 10
    }

    init {
        plugin.saveDefaultConfig()
        getConfig(Files.MESSAGES)
    }

    /**
     * Updates or initializes the state for a sender, leveraging StateManager.
     */
    fun initializeState(sender: CommandSender, code: String? = null): RedeemCodeState {
        return stateManager.getOrCreateState(sender, code)
    }

    /**
     * Sends a message to the sender with support for placeholders and multiple message formats.
     */
    fun sendMessage(key: String, state: RedeemCodeState) {
        // Placeholder map for message customization
        val placeholders = state.toPlaceholdersMap()

        // Fetch different types of messages
        val chatMessage = getString("$key.chat") ?: getString(key)
        val actionBarMessage = getString("$key.actionbar")
        val titleMessage = getString("$key.title.text") ?: getString("$key.title")
        val subtitleMessage = getString("$key.title.subtitle")
        val fadeIn = getString("$key.title.fade-in")?.toIntOrNull() ?: DEFAULT_FADE_IN
        val stay = getString("$key.title.stay")?.toIntOrNull() ?: DEFAULT_STAY
        val fadeOut = getString("$key.title.fade-out")?.toIntOrNull() ?: DEFAULT_FADE_OUT

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
            val filledMessage = applyPlaceholders(it, placeholders)
            state.sender.sendMessage(filledMessage)
        }
    }

    private fun applyPlaceholders(message: String, placeholders: Map<String, String>): String {
        return placeholders.entries.fold(message) { msg, (placeholder, value) ->
            msg.replace("{$placeholder}", value)
        }
    }

    fun getConfig(configFile: Files): FileConfiguration {
        return configFiles.computeIfAbsent(configFile) {
            val file = File(plugin.dataFolder, configFile.filename)
            if (!file.exists()) plugin.saveResource(configFile.filename, false)
            YamlConfiguration.loadConfiguration(file)
        }
    }

    fun saveConfig(configFile: Files) {
        try {
            val file = File(plugin.dataFolder, configFile.filename)
            getConfig(configFile).save(file)
            plugin.logger.log(Level.INFO, "${configFile.filename} saved successfully.")
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not save ${configFile.filename}: ${e.message}")
        }
    }

    fun reloadConfig(configFile: Files) {
        try {
            configFiles[configFile] = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, configFile.filename))
            plugin.logger.log(Level.INFO, "${configFile.filename} reloaded successfully.")
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not reload ${configFile.filename}: ${e.message}")
        }
    }

    fun getString(key: String, configFile: Files = Files.MESSAGES, applyColor: Boolean = true): String? {
        val fileConfig = getConfig(configFile)
        val message = fileConfig.getString(key) ?: return null
        return if (applyColor) applyColors(message) else message
    }

    private fun applyColors(message: String): String {
        var coloredMessage = ChatColor.translateAlternateColorCodes('&', message)
        val hexPattern = Pattern.compile("&#[a-fA-F0-9]{6}")
        val matcher = hexPattern.matcher(coloredMessage)
        while (matcher.find()) {
            val hexCode = matcher.group()
            val bukkitHexCode = formatHexColor(hexCode)
            coloredMessage = coloredMessage.replace(hexCode, bukkitHexCode)
        }
        return coloredMessage
    }

    private fun formatHexColor(hexCode: String): String {
        return "\u00A7x" + hexCode.substring(2).toCharArray().joinToString("") { "\u00A7$it" }
    }

    fun saveAllConfigs() {
        configFiles.keys.forEach { saveConfig(it) }
    }

    fun reloadAllConfigs() {
        configFiles.keys.forEach { reloadConfig(it) }
    }
}
