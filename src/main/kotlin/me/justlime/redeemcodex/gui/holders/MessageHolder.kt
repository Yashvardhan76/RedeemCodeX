package me.justlime.redeemcodex.gui.holders

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.enums.RedeemType
import me.justlime.redeemcodex.gui.InventoryManager
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.models.MessageState
import me.justlime.redeemcodex.models.Title
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class MessageHolder(
    private val plugin: RedeemCodeX, player: Player, private val redeemData: RedeemType, row: Int, title: String
) : InventoryHolder, GUIHandle {

    private val inventory = Bukkit.createInventory(this, row * 9, title)
    private val placeholder = when (redeemData) {
        is RedeemType.Code -> CodePlaceHolder.applyByRedeemCode(redeemData.redeemCode, player)
        is RedeemType.Template -> CodePlaceHolder.applyByTemplate(redeemData.redeemTemplate, player)
    }
    private val messageState = MessageState(mutableListOf(), "", Title())
    private var inputReceived = false

    override fun getInventory(): Inventory = inventory

    override fun loadContent() {
        inventory.clear()
        InventoryManager.outlineInventory(inventory, 3)
        loadMainMenu()
        val mainItem = ItemStack(Material.NETHER_STAR).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("§aEditing : §b${getRedeemName()}")
            }
        }
        inventory.setItem(22, mainItem)
    }

    private fun getRedeemName(): String = when (redeemData) {
        is RedeemType.Code -> redeemData.redeemCode.code
        is RedeemType.Template -> redeemData.redeemTemplate.name
    }

    override fun onClick(event: InventoryClickEvent, clickedInventory: Inventory, player: Player) {
        event.isCancelled = true
        when (event.currentItem?.type) {
            Material.PAPER -> openInputText(player)
            Material.FEATHER -> openInputActionBar(player)
            Material.BOOK -> openInputTitle(player)
            Material.NETHER_STAR -> upsertMessage()
            else -> return
        }
    }

    override fun onOpen(event: InventoryOpenEvent, player: Player) {
        loadContent()
    }

    override fun onClose(event: InventoryCloseEvent, player: Player) {}

    private fun loadMainMenu() {
        inventory.setItem(10, createMenuItem(Material.PAPER, "§aChat Messages", "§eClick to Edit"))
        inventory.setItem(13, createMenuItem(Material.FEATHER, "§aAction Bar Messages", "§eClick to Edit"))
        inventory.setItem(16, createMenuItem(Material.BOOK, "§aTitle Messages", "§eClick to Edit"))
    }

    private fun createMenuItem(material: Material, name: String, lore: String): ItemStack {
        return ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName(name)
                this.lore = listOf(lore)
            }
        }
    }

    private fun openInputText(player: Player) {
        inputReceived = false
        closeInventoryWithMessage(player, "§eEnter your messages in chat. Type 'done' when you're finished:")
        plugin.listenerManager.asyncPlayerChatListener.registerCallback(player, InventoryManager.timeOut, onTimeout = {
            handleInputTimeout(player, "§cInput timed out. Returning to menu.")
        }, callback = { message ->
            if (message.equals("done", ignoreCase = true)) {
                finalizeInput(player, "§aChat messages updated.")
                inputReceived = true
                player.openInventory(inventory)
            } else {
                val formattedMessage = JService.applyColors(message)
                messageState.text.add(formattedMessage)
                player.sendMessage("§aAdded: $formattedMessage")
            }

        })
    }

    private fun openInputActionBar(player: Player) {
        inputReceived = false
        closeInventoryWithMessage(player, "§eEnter your action bar message:")

        plugin.listenerManager.asyncPlayerChatListener.registerCallback(player, InventoryManager.timeOut, {
            handleInputTimeout(player, "§cAction bar message input timed out.")
        }, callback = { message ->
            messageState.actionbar = JService.applyColors(message)
            finalizeInput(player, "§aActionbar message set to: ${messageState.actionbar}")
        })
    }

    private fun openInputTitle(player: Player) {
        inputReceived = false
        closeInventoryWithMessage(player, "§eEnter your title message (format: title;subtitle;fadeIn;stay;fadeOut):")

        plugin.listenerManager.asyncPlayerChatListener.registerCallback(player, InventoryManager.timeOut, {
            handleInputTimeout(player, "§cTitle message input timed out.")
        }, callback = { message ->
            val parts = message.split(";")
            if (parts.size == 5) {
                val title = parseTitleInput(parts)
                if (title != null) {
                    messageState.title = title
                    finalizeInput(player, "§aTitle message updated.")
                } else {
                    player.sendMessage("§cInvalid number format. Please try again.")
                }
            } else {
                player.sendMessage("§cInvalid format. Use: title;subtitle;fadeIn;stay;fadeOut")
            }
        })
    }

    private fun parseTitleInput(parts: List<String>): Title? {
        return try {
            Title(
                title = JService.applyColors(parts[0]),
                subTitle = JService.applyColors(parts[1]),
                fadeIn = parts[2].toInt(),
                stay = parts[3].toInt(),
                fadeOut = parts[4].toInt()
            )
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun handleInputTimeout(player: Player, message: String) {
        plugin.listenerManager.asyncPlayerChatListener.unregisterCallback(player)
        player.sendMessage(message)
        reopenInventory(player)
    }

    private fun finalizeInput(player: Player, message: String) {
        inputReceived = true
        player.sendMessage(message)
        reopenInventory(player)
    }

    private fun reopenInventory(player: Player) {
        plugin.server.scheduler.runTask(plugin, Runnable {
            player.openInventory(inventory)
            loadContent()
        })
    }

    private fun closeInventoryWithMessage(player: Player, message: String) {
        player.closeInventory()
        player.sendMessage(message)
    }

    private fun upsertMessage() {
        when (redeemData) {
            is RedeemType.Code -> {
                redeemData.redeemCode.messages = messageState
                plugin.redeemCodeDB.upsertCode(redeemData.redeemCode)
            }

            is RedeemType.Template -> {
                redeemData.redeemTemplate.messages = messageState
                plugin.config.upsertTemplate(redeemData.redeemTemplate)
            }
        }
    }
}
