/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 */

package me.justlime.redeemcodex.utilities

import me.justlime.redeemcodex.RedeemCodeX
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class JLogger(private val plugin: RedeemCodeX) {

    private val logFolder: File = File(plugin.dataFolder, "logs")
    private val logFile: File

    // Logger settings
    private val logGenerate: Boolean
    private val logModify: Boolean
    private val logDelete: Boolean
    private val logRedeemed: Boolean

    init {
        // Load logging preferences from config.yml
        val config = plugin.configRepo
        logGenerate = config.getConfigValue("logger.generate").toBoolean()
        logModify = config.getConfigValue("logger.modify").toBoolean()
        logDelete = config.getConfigValue("logger.delete").toBoolean()
        logRedeemed = config.getConfigValue("logger.redeemed").toBoolean()

        // Create the logs directory if it doesn't exist
        if (!logFolder.exists() && !logFolder.mkdirs()) {
            plugin.logger.warning("Failed to create logs folder!")
        }

        // Create the daily log file
        val fileName = "${SimpleDateFormat("yyyy-MM-dd-HH").format(Date())}.txt"
        logFile = File(logFolder, fileName)

        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile()) {
                    plugin.logger.warning("Failed to create log file: ${logFile.name}")
                }
            } catch (e: IOException) {
                plugin.logger.severe("Error creating log file: ${e.message}")
            }
        }
    }

    /**
     * Logs a generate event if enabled.
     */
    fun logGenerate(message: String, sender: String) {
        if (logGenerate) logToFile("GENERATE", message, sender)
    }

    /**
     * Logs a modify event if enabled.
     */
    fun logModify(message: String, sender: String) {
        if (logModify) logToFile("MODIFY", message, sender)
    }

    /**
     * Logs a delete event if enabled.
     */
    fun logDelete(message: String, sender: String) {
        if (logDelete) logToFile("DELETE", message, sender)
    }

    fun logRedeemed(message: String, sender: String) {
        if (logRedeemed) logToFile("REDEEMED", message, sender)
    }

    /**
     * Generic logging function with a tag.
     */
    private fun logToFile(tag: String, message: String, sender: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val logMessage = "[$timestamp] [$tag] $message by $sender"

        // Append to log file
        try {
            FileWriter(logFile, true).use { writer ->
                writer.appendLine(logMessage)
            }
            DiscordLogger.sendDiscordLog(logMessage)
        } catch (e: IOException) {
            plugin.logger.severe("Failed to write to log file: ${e.message}")
        }
    }
}
