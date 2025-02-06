package me.justlime.redeemcodex.utilities

import me.justlime.redeemcodex.api.RedeemXAPI
import me.justlime.redeemcodex.enums.JFiles
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets

object DiscordLogger {
    private val plugin = RedeemXAPI.getPlugin()
    private val isWebhookEnabled = plugin.configManager.getConfig(JFiles.CONFIG).getBoolean("logger.webhook.enabled")
    private val WEBHOOK_URL = plugin.configManager.getConfig(JFiles.CONFIG).getString("logger.webhook.url")
    private val messagesQueue = mutableListOf<String>()

    fun sendDiscordLog(message: String) {
        if (!isWebhookEnabled || WEBHOOK_URL.isNullOrBlank() || WEBHOOK_URL == "YOUR_DISCORD_WEBHOOK_URL") return
        synchronized(messagesQueue) {
            messagesQueue.add(message)
        }
    }

    //run every 5s
    fun run() {
        synchronized(messagesQueue) {
            if (messagesQueue.isEmpty()) return

            // Split the messages into chunks based on the 2000 character limit
            val messageChunks = splitMessagesIntoChunks(messagesQueue)

            // Send the chunks asynchronously
            messageChunks.forEach { chunk ->
                sendMessagesToDiscord(chunk)
            }

            // Clear the queue after processing
            messagesQueue.clear()
        }
    }

    // Function to split the list of messages into chunks based on the 2000 character limit
    private fun splitMessagesIntoChunks(messages: MutableList<String>): List<List<String>> {
        val chunkedMessages = mutableListOf<List<String>>()
        var currentChunk = mutableListOf<String>()
        var currentLength = 0

        for (message in messages) {
            val messageLength = message.length
            if (currentLength + messageLength > 1800) {
                chunkedMessages.add(currentChunk)  // Add the current chunk to the list
                currentChunk = mutableListOf()     // Start a new chunk
                currentLength = 0
            }
            currentChunk.add(message)
            currentLength += messageLength
        }

        // Add the last chunk if there are any remaining messages
        if (currentChunk.isNotEmpty()) {
            chunkedMessages.add(currentChunk)
        }

        return chunkedMessages
    }

    private fun sendMessagesToDiscord(messageChunk: List<String>) {
        val message = messageChunk.joinToString("\\n")
        sendToDiscord(message)
    }

    private fun sendToDiscord(message: String) {
        var connection: HttpURLConnection? = null
        try {
            val url = URI.create(WEBHOOK_URL).toURL()
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            val jsonPayload = """{"content": "$message"}"""
            connection.outputStream.use { it.write(jsonPayload.toByteArray(StandardCharsets.UTF_8)) }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                plugin.logger.warning("Discord webhook request failed with response code: $responseCode")
            }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to send log to Discord: ${e.message}")
        }
    }
}
