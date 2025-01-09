package me.justlime.redeemcodex.gui.holders

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.commands.subcommands.ModifySubCommand
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.data.repository.RedeemCodeRepository
import me.justlime.redeemcodex.enums.JFiles
import me.justlime.redeemcodex.enums.RedeemType
import me.justlime.redeemcodex.gui.InventoryManager
import me.justlime.redeemcodex.models.SoundState
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

class SoundHolder(val plugin: RedeemCodeX, val player: Player, private val redeemData: RedeemType, row: Int, title: String) : InventoryHolder,
    GUIHandle {
    private val inventory = Bukkit.createInventory(this, row * 9, title)
    private val musicDiscs = listOf(
        Material.MUSIC_DISC_13,
        Material.MUSIC_DISC_BLOCKS,
        Material.MUSIC_DISC_CAT,
        Material.MUSIC_DISC_CHIRP,
        Material.MUSIC_DISC_FAR,
        Material.MUSIC_DISC_MALL,
        Material.MUSIC_DISC_MELLOHI,
        Material.MUSIC_DISC_STAL,
        Material.MUSIC_DISC_STRAD,
        Material.MUSIC_DISC_WAIT,
        Material.MUSIC_DISC_WARD
    )
    private var currentPage = 0
    private val itemsPerPage = InventoryManager.selectedSlots.size
    private var currentSearch: String? = null
    private var selectedSlot: Int? = null
    private var selectedSound: SoundState = when (redeemData) {
        is RedeemType.Code -> redeemData.redeemCode.sound
        is RedeemType.Template -> SoundState(
            sound = try {
                Sound.valueOf(redeemData.redeemTemplate.sound)
            } catch (e: Exception) {
                null
            }, volume = redeemData.redeemTemplate.soundVolume, pitch = redeemData.redeemTemplate.soundPitch
        )
    }
    private var pitch = selectedSound.pitch
    private var volume = selectedSound.volume
    private var sortMode = SortMode.FAVORITE
    private var recentSounds: MutableList<String> = mutableListOf()
    private var favouriteSounds: MutableList<String> = mutableListOf()

    override fun getInventory(): Inventory = inventory

    override fun loadContent() {
        inventory.clear()
        InventoryManager.outlineInventory(inventory)
        loadSounds()
        val soundEntries = getSortedSounds()
//        plugin.logger.info("$sortMode - ${soundEntries.size} - (${currentPage})")

        val startIndex = currentPage * itemsPerPage
        val endIndex = (startIndex + itemsPerPage).coerceAtMost(soundEntries.size)
        val maxPages = (soundEntries.size + itemsPerPage - 1) / itemsPerPage

        var slotIndex = 0
        for (i in startIndex until endIndex) {
            val sound = soundEntries[i]
            val disc = ItemStack(musicDiscs[i % musicDiscs.size])
            val meta = disc.itemMeta ?: continue
            meta.setDisplayName(sound.name)
            val loreList: MutableList<String> = mutableListOf("§aClick to Select")
            if (slotIndex == selectedSlot) {
                loreList[0] = "§aSelected"
            }
            if (meta.displayName in favouriteSounds) loreList.add("§aFavourite") else loreList.remove("§aFavourite")
            meta.lore = loreList
            disc.itemMeta = meta
            inventory.setItem(InventoryManager.selectedSlots[slotIndex++], disc)
            if (slotIndex >= InventoryManager.selectedSlots.size) break
        }

        // Navigation and control items
        val backArrow = ItemStack(Material.ARROW).apply { itemMeta = itemMeta?.apply { setDisplayName("§ePrevious Page") } }
        val forwardArrow = ItemStack(Material.ARROW).apply { itemMeta = itemMeta?.apply { setDisplayName("§eNext Page") } }
        val netherStar = ItemStack(Material.NETHER_STAR).apply {
            itemMeta = itemMeta?.apply { setDisplayName("§aSave Selected Sound") }
        }
        val pitchItem = ItemStack(Material.REDSTONE).apply { itemMeta = itemMeta?.apply { setDisplayName("§aPitch: $pitch") } }
        val volumeItem = ItemStack(Material.GLOWSTONE_DUST).apply { itemMeta = itemMeta?.apply { setDisplayName("§aVolume: $volume") } }
        val searchItem = ItemStack(Material.BOOK).apply { itemMeta = itemMeta?.apply { setDisplayName("§aSearch Sound") } }
        val sortItem = ItemStack(Material.HOPPER).apply { itemMeta = itemMeta?.apply { setDisplayName("§aSort: ${sortMode.displayName}") } }

        if (currentPage > 0) inventory.setItem(48, backArrow)
        if (currentPage < maxPages - 1) inventory.setItem(50, forwardArrow)
        inventory.setItem(49, netherStar)
        inventory.setItem(46, pitchItem)
        inventory.setItem(47, volumeItem)
        inventory.setItem(51, searchItem)
        inventory.setItem(52, sortItem)
    }

    override fun onClick(event: InventoryClickEvent, clickedInventory: Inventory, player: Player) {
        event.isCancelled = true
        player.stopAllSounds()

        if (event.clickedInventory != inventory) return

        val clickedItem = event.currentItem ?: return
        when (clickedItem.type) {
            Material.ARROW -> handleArrowClick(event.slot)
            Material.NETHER_STAR -> handleSaveClick(player)
            Material.REDSTONE -> adjustPitch(event.click.isLeftClick)
            Material.GLOWSTONE_DUST -> adjustVolume(event.click.isLeftClick)
            Material.BOOK -> openSearchInput(player)
            Material.HOPPER -> cycleSortMode(event.click)
            in musicDiscs -> handleDiscClick(event.click, event.slot, clickedItem, player)
            else -> return
        }
    }

    override fun onOpen(event: InventoryOpenEvent, player: Player) {
        loadContent()
    }

    override fun onClose(event: InventoryCloseEvent, player: Player) {
        return
    }

    private fun handleArrowClick(slot: Int) {
        val soundEntries = getSortedSounds()
        val maxPages = (soundEntries.size + itemsPerPage - 1) / itemsPerPage
        when (slot) {
            48 -> if (currentPage > 0) {
                currentPage--
                loadContent()
            }

            50 -> if (currentPage < maxPages - 1) {
                currentPage++
                loadContent()
            }
        }
    }

    private fun handleSaveClick(player: Player) {
        player.sendMessage("§aSelected Sound Saved! $pitch $volume")
        val codeRepository = RedeemCodeRepository(plugin)
        val configRepository = ConfigRepository(plugin)
        if (redeemData is RedeemType.Code) codeRepository.upsertCode(redeemData.redeemCode)
        else if (redeemData is RedeemType.Template) {
            configRepository.upsertTemplate(redeemData.redeemTemplate)
            ModifySubCommand(plugin).execute(player, mutableListOf("modify", "template", redeemData.redeemTemplate.name, "sync"))
        }
        val soundName = selectedSound.sound?.name ?: return
        saveRecentSound(soundName)
        player.closeInventory()
    }

    private fun handleDiscClick(clickEvent: ClickType, slot: Int, clickedItem: ItemStack, player: Player) {
        if (selectedSlot == slot && !clickEvent.isShiftClick) {
            selectedSound.playSound(player)
            return
        }
        val soundName = clickedItem.itemMeta?.displayName ?: return
        val sound = Sound.entries.find { it.name == soundName } ?: return
        if (clickEvent.isShiftClick) {
            if (sortMode == SortMode.FAVORITE) {
                removeFavouriteSound(soundName)
                inventory.setItem(slot, ItemStack(Material.AIR))
                loadContent()
                return
            }

            if (soundName in favouriteSounds) return
            saveFavouriteSound(soundName)
            loadSounds()
            val meta = clickedItem.itemMeta
            val loreList: MutableList<String> = mutableListOf("§aClick to Select")
            if (meta?.displayName in favouriteSounds) loreList.add("§aFavourite") else loreList.remove("§aFavourite")
            meta?.lore = loreList
            clickedItem.itemMeta = meta
            return
        }
        selectedSlot?.let { deselectSlot(it) }
        selectedSlot = slot
        val meta = clickedItem.itemMeta
        meta?.addEnchant(Enchantment.UNBREAKING, 1, true)
        meta?.lore = listOf("§aSelected")
        clickedItem.itemMeta = meta
        selectedSound = SoundState(sound, volume, pitch)
        selectedSound.playSound(player)

        when (redeemData) {
            is RedeemType.Code -> {
                redeemData.redeemCode.sound = selectedSound
            }

            is RedeemType.Template -> {
                redeemData.redeemTemplate.sound = selectedSound.sound?.name ?: return
                redeemData.redeemTemplate.soundVolume = volume
                redeemData.redeemTemplate.soundPitch = pitch
            }
        }
    }

    private fun adjustPitch(isAdding: Boolean) {
        pitch = BigDecimal(
            pitch + if (isAdding && pitch < 2.0) 0.1
            else if (!isAdding && pitch > 0.1) -0.1
            else 0.0
        ).setScale(1, RoundingMode.HALF_UP).toFloat()
        selectedSound.pitch = pitch
        selectedSound.playSound(player)
        loadContent()
    }

    private fun adjustVolume(isAdding: Boolean) {
        volume = BigDecimal(
            volume + if (isAdding && volume < 2.0) 0.1
            else if (!isAdding && volume > 0.1) -0.1
            else 0.0
        ).setScale(1, RoundingMode.HALF_UP).toFloat()
        selectedSound.volume = volume
        selectedSound.playSound(player)
        loadContent()
    }

    private fun deselectSlot(slot: Int) {
        val item = inventory.getItem(slot) ?: return
        item.itemMeta = item.itemMeta?.apply { lore = mutableListOf("§aClick to Select"); removeEnchantments(); }
        inventory.setItem(slot, item)
    }

    private fun openSearchInput(player: Player) {
        player.closeInventory()
        player.sendMessage("§eEnter your search query in chat:")
        currentSearch = null
        val listener = plugin.listenerManager.asyncPlayerChatListener
        listener.registerCallback(player) { query ->
            currentSearch = query
            player.sendMessage("§aSearch query set to: $query")
            plugin.server.scheduler.runTask(plugin, Runnable {
                currentPage = 0
                sortMode = SortMode.SEARCH
                player.openInventory(inventory)
                loadContent()
            })
        }
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (currentSearch == null) {
                listener.unregisterCallback(player)
                player.sendMessage("§cSearch timed out.")
                player.openInventory(inventory)
            }
        }, InventoryManager.timeOut * 20)

    }

    private fun cycleSortMode(click: ClickType) {
        loadSounds()
        if (click.isLeftClick) sortMode = sortMode.next()
        else if (click.isRightClick) sortMode = sortMode.prev()
        currentPage = 0
        loadContent()
    }

    private fun getSortedSounds(): List<Sound> {
        val sounds = currentSearch?.let { search ->
            Sound.entries.filter { it.name.contains(search, ignoreCase = true) }
        } ?: Sound.entries.toList()

        return when (sortMode) {
            SortMode.ASCENDING -> sounds.sortedBy { it.name }
            SortMode.DESCENDING -> sounds.sortedByDescending { it.name }
            SortMode.FAVORITE -> sounds.filter { it.name in favouriteSounds }.sortedBy { favouriteSounds.indexOf(it.name) }
            SortMode.RECENT -> sounds.filter { it.name in recentSounds }.sortedBy { recentSounds.indexOf(it.name) }
            SortMode.SEARCH -> {
                currentSearch?.let { search ->
                    sounds.filter { it.name.contains(search, ignoreCase = true) }
                } ?: sounds
            }
        }
    }

    private fun loadSounds() {
        val guiConfig = plugin.configManager.getConfig(JFiles.GUI)
        val filter = guiConfig.getConfigurationSection("filter") ?: return
        recentSounds = filter.getStringList("recent-sounds").toMutableList()
        recentSounds.reverse()
        favouriteSounds = filter.getStringList("favourite").toMutableList()
        favouriteSounds.reverse()
    }

    private fun saveRecentSound(soundName: String) {
        if (!recentSounds.contains(soundName)) {
            recentSounds.add(soundName)
            if (recentSounds.size > 56) recentSounds.removeAt(0) // Keep only the last 10
        }
        val config = plugin.configManager.getConfig(JFiles.GUI)
        val section = config.getConfigurationSection("filter")
        recentSounds.let { section?.set("recent-sounds", it) }
        config.save(File(plugin.dataFolder, JFiles.GUI.filename))
    }

    private fun saveFavouriteSound(soundName: String) {
        if (!favouriteSounds.contains(soundName)) {
            favouriteSounds.add(soundName)
        }
        val config = plugin.configManager.getConfig(JFiles.GUI)
        val section = config.getConfigurationSection("filter")
        favouriteSounds.let { section?.set("favourite", it) }
        config.save(File(plugin.dataFolder, JFiles.GUI.filename))
    }

    private fun removeFavouriteSound(soundName: String) {
        if (!favouriteSounds.contains(soundName)) return
        favouriteSounds.remove(soundName)
        val config = plugin.configManager.getConfig(JFiles.GUI)
        val section = config.getConfigurationSection("filter")
        favouriteSounds.let { section?.set("favourite", it) }
        config.save(File(plugin.dataFolder, JFiles.GUI.filename))
    }

    private enum class SortMode(val displayName: String) {
        FAVORITE("Favorites"), RECENT("Recent"), ASCENDING("Ascending"), DESCENDING("Descending"), SEARCH("Search");

        fun next(): SortMode = entries[(ordinal + 1) % entries.size].also { if (it == SEARCH) it.next() }
        fun prev(): SortMode = entries[(ordinal + entries.size - 1) % entries.size].also { if (it == SEARCH) it.prev() }

    }
}
