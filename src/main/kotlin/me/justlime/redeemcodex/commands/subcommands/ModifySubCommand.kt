/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 *
 */


package me.justlime.redeemcodex.commands.subcommands

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.commands.JSubCommand
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.data.repository.RedeemCodeRepository
import me.justlime.redeemcodex.enums.JMessage
import me.justlime.redeemcodex.enums.JPermission
import me.justlime.redeemcodex.enums.JTab
import me.justlime.redeemcodex.enums.JTemplate
import me.justlime.redeemcodex.enums.RedeemType
import me.justlime.redeemcodex.gui.InventoryManager
import me.justlime.redeemcodex.gui.holders.MessageHolder
import me.justlime.redeemcodex.gui.holders.RewardsHolder
import me.justlime.redeemcodex.gui.holders.SoundHolder
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.models.RedeemCode
import me.justlime.redeemcodex.models.RedeemTemplate
import me.justlime.redeemcodex.utilities.JLogger
import me.justlime.redeemcodex.utilities.JService
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

//rcx modify code <code> <toggle-property> [codes]
//rcx modify code <code> <property> <value> ([codes] //Except SetCommand, AddCommand)
//rcx modify template <template> <toggle-property> [codes]
//rcx modify template <template> <property> <value> ([codes] //Except SetCommand, AddCommand)

class ModifySubCommand(private val plugin: RedeemCodeX) : JSubCommand {
    private val config = ConfigRepository(plugin)
    private val codeRepo = RedeemCodeRepository(plugin)
    lateinit var placeHolder: CodePlaceHolder
    lateinit var sender: CommandSender
    override var jList: List<String> = emptyList()
    override val permission: String = JPermission.Admin.MODIFY
    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        placeHolder = CodePlaceHolder(sender)
        this.sender = sender
        if (!hasPermission(sender)) return sendMessage(JMessage.Command.NO_PERMISSION)
        if (args.size < 3) return !sendMessage(JMessage.Command.UNKNOWN_COMMAND)
        val type = args[1]
        return when (type) {
            JTab.Type.CODE -> codeModification(sender, args)
            JTab.Type.TEMPLATE -> templateModification(sender, args)
            else -> return !sendMessage(JMessage.Command.UNKNOWN_COMMAND)
        }
    }

    override fun tabCompleter(sender: CommandSender, args: MutableList<String>): MutableList<String>? {
        val cachedCodes = codeRepo.getCachedCode()
        val cachedTemplate = config.getAllTemplates().map { it.name }
        val cachedTargetList = codeRepo.getCachedTargetList()
        val modifyOptions = mutableListOf(
            JTab.Modify.ENABLED,
            JTab.Modify.SYNC,
            JTab.Modify.SET_REDEMPTION,
            JTab.Modify.SET_PLAYER_LIMIT,
            JTab.Modify.SET_COMMAND,
            JTab.Modify.ADD_COMMAND,
            JTab.Modify.REMOVE_COMMAND,
            JTab.Modify.LIST_COMMAND,
            JTab.Modify.SET_DURATION,
            JTab.Modify.ADD_DURATION,
            JTab.Modify.REMOVE_DURATION,
            JTab.Modify.SET_PERMISSION,
            JTab.Modify.SET_PIN,
            JTab.Modify.SET_TARGET,
            JTab.Modify.ADD_TARGET,
            JTab.Modify.REMOVE_TARGET,
            JTab.Modify.LIST_TARGET,
            JTab.Modify.SET_COOLDOWN,
            JTab.Modify.SET_TEMPLATE,
            JTab.Modify.Edit.REWARD,
            JTab.Modify.Edit.MESSAGE,
            JTab.Modify.Edit.SOUND,
        )
        val templateOptions = mutableListOf(
            JTab.Modify.ENABLED,
            JTab.Modify.SYNC,
            JTab.Modify.SET_REDEMPTION,
            JTab.Modify.SET_PLAYER_LIMIT,
            JTab.Modify.SET_COMMAND,
            JTab.Modify.ADD_COMMAND,
            JTab.Modify.REMOVE_COMMAND,
            JTab.Modify.LIST_COMMAND,
            JTab.Modify.SET_DURATION,
            JTab.Modify.ADD_DURATION,
            JTab.Modify.REMOVE_DURATION,
            JTab.Modify.SET_PERMISSION,
            JTab.Modify.REQUIRED_PERMISSION,
            JTab.Modify.SET_PIN,
            JTab.Modify.SET_COOLDOWN,
            JTab.Modify.SET_TEMPLATE,
            JTab.Modify.Edit.REWARD,
            JTab.Modify.Edit.MESSAGE,
            JTab.Modify.Edit.SOUND,
        )
        val completions = mutableListOf<String>()

        if (!hasPermission(sender)) return mutableListOf()
        when (args.size) {
            2 -> {
                completions.addAll(mutableListOf(JTab.Type.CODE, JTab.Type.TEMPLATE))
            }

            3 -> {
                if (args[1] == JTab.Type.CODE) completions.addAll(cachedCodes)
                if (args[1] == JTab.Type.TEMPLATE) completions.addAll(cachedTemplate)
            }

            4 -> {
                if (args[1] == JTab.Type.CODE) completions.addAll(modifyOptions)
                if (args[1] == JTab.Type.TEMPLATE) completions.addAll(templateOptions)
            }

            5 -> {
                if (args[1] == JTab.Type.CODE) when (args[3]) {
                    JTab.Modify.SET_TARGET, JTab.Modify.ADD_TARGET -> return null
                    JTab.Modify.REMOVE_TARGET -> completions.addAll(cachedTargetList[args[2]] ?: emptyList())
                    //TODO Add Cached ID
                    JTab.Modify.SET_COMMAND -> completions.add("ID")
                    JTab.Modify.REMOVE_COMMAND -> completions.add("ID")
                    JTab.Modify.EDIT -> {
                        completions.add(JTab.Modify.Edit.REWARD)
                        completions.add(JTab.Modify.Edit.MESSAGE)
                        completions.add(JTab.Modify.Edit.SOUND)
                    }

                    JTab.Modify.SET_TEMPLATE -> completions.addAll(cachedTemplate)
                    else -> return mutableListOf()
                }
                if (args[1] == JTab.Type.TEMPLATE) when (args[3]) {
                    JTab.Modify.SET_COMMAND -> completions.add("ID")
                    JTab.Modify.REMOVE_COMMAND -> completions.add("ID")
                    JTab.Modify.EDIT -> {
                        completions.add(JTab.Modify.Edit.REWARD)
                        completions.add(JTab.Modify.Edit.MESSAGE)
                        completions.add(JTab.Modify.Edit.SOUND)
                    }
                }

            }

            else -> {
                if (args[1] == JTab.Type.CODE) {
                    when (args[3]) {
                        JTab.Modify.SET_TARGET, JTab.Modify.ADD_TARGET -> return null
                        JTab.Modify.REMOVE_TARGET -> completions.addAll((cachedTargetList[args[2]] ?: emptyList()).filter { it !in args })
                    }
                }
            }

        }
        return completions
    }

    override fun sendMessage(key: String): Boolean {
        if (placeHolder.sentMessage.isBlank()) placeHolder.sentMessage = config.getMessage(key, placeHolder)
        else placeHolder.sentMessage = "${placeHolder.sentMessage}\n ${config.getMessage(key, placeHolder)}"
        config.sendMsg(key, placeHolder)
        return true
    }

    private fun codeModification(sender: CommandSender, args: MutableList<String>): Boolean {
        val code = args[2].uppercase()
        val redeemCode = codeRepo.getCode(code) ?: return !sendMessage(JMessage.Code.NOT_FOUND)

        placeHolder = CodePlaceHolder.fetchByDB(plugin, code, sender).also { it.property = args[3] }
        jList = listOf(code)

        val options = mutableListOf(
            JTab.Modify.ENABLED,
            JTab.Modify.SYNC,
            JTab.Modify.LIST_TARGET,
            JTab.Modify.LIST_COMMAND,
            JTab.Modify.Edit.REWARD,
            JTab.Modify.Edit.MESSAGE,
            JTab.Modify.Edit.SOUND,
        )
        val optionsWithValue = mutableListOf(
            JTab.Modify.SET_TEMPLATE,
            JTab.Modify.SET_DURATION,
            JTab.Modify.ADD_DURATION,
            JTab.Modify.REMOVE_DURATION,
            JTab.Modify.SET_COOLDOWN,
            JTab.Modify.SET_REDEMPTION,
            JTab.Modify.SET_PLAYER_LIMIT,
            JTab.Modify.SET_PERMISSION,
        )
        val optionsWithValues = mutableListOf(
            JTab.Modify.SET_TARGET,
            JTab.Modify.ADD_TARGET,
            JTab.Modify.REMOVE_TARGET,
            JTab.Modify.SET_COMMAND,
            JTab.Modify.ADD_COMMAND,
            JTab.Modify.REMOVE_COMMAND,
        )

        if (placeHolder.property in optionsWithValue && args.size < 5) return sendMessage(JMessage.Code.Modify.INVALID_VALUE)
        if (placeHolder.property in optionsWithValues && args.size < 5) return sendMessage(JMessage.Code.Modify.INVALID_VALUE)

        when (placeHolder.property) {
            in options -> return modify(redeemCode, placeHolder.property)
            in optionsWithValue -> return modify(redeemCode, placeHolder.property, args[4])
            in optionsWithValues -> return modify(redeemCode, placeHolder.property, args.drop(4).toMutableList())
        }
        return sendMessage(JMessage.Command.UNKNOWN_COMMAND)
    }

    private fun templateModification(sender: CommandSender, args: MutableList<String>): Boolean {
        val template = args[2].uppercase()
        val redeemTemplate = config.getTemplate(template) ?: return sendMessage(JMessage.Template.NOT_FOUND)
        if (args.size < 4 && sender is Player) return InventoryManager.openTemplateSetting(sender, redeemTemplate)
        if (args.size < 4) return !sendMessage(JMessage.Command.UNKNOWN_COMMAND)
        placeHolder = CodePlaceHolder.applyByTemplate(redeemTemplate, sender).also { it.property = args[3] }

        jList = listOf(template)
        val options = mutableListOf(
            JTab.Modify.ENABLED,
            JTab.Modify.SYNC,
            JTab.Modify.SET_PERMISSION,
            JTab.Modify.LIST_TARGET,
            JTab.Modify.LIST_COMMAND,
            JTab.Modify.Edit.REWARD,
            JTab.Modify.Edit.MESSAGE,
            JTab.Modify.Edit.SOUND,
        )
        val optionsWithValue = mutableListOf(
            JTab.Modify.SET_DURATION,
            JTab.Modify.ADD_DURATION,
            JTab.Modify.REMOVE_DURATION,
            JTab.Modify.SET_COOLDOWN,
            JTab.Modify.SET_REDEMPTION,
            JTab.Modify.SET_PLAYER_LIMIT,
            JTab.Modify.SET_PIN,

            )
        val optionsWithValues = mutableListOf(
            JTab.Modify.SET_COMMAND,
            JTab.Modify.ADD_COMMAND,
            JTab.Modify.REMOVE_COMMAND,
        )

        if (placeHolder.property in optionsWithValue && args.size < 5) return sendMessage(JMessage.Template.Modify.INVALID_VALUE)
        if (placeHolder.property in optionsWithValues && args.size < 5) return sendMessage(JMessage.Template.Modify.INVALID_VALUE)

        when (placeHolder.property) {
            in options -> return modify(redeemTemplate, placeHolder.property)
            in optionsWithValue -> return modify(redeemTemplate, placeHolder.property, args[4])
            in optionsWithValues -> return modify(redeemTemplate, placeHolder.property, args.drop(4).toMutableList())
        }
        return false
    }

    private fun modify(redeemCode: RedeemCode, property: String): Boolean {
        return when (property) {
            JTab.Modify.ENABLED -> toggleEnabledStatus(redeemCode)
            JTab.Modify.SYNC -> toggleTemplateSyncStatus(redeemCode)
            JTab.Modify.SET_PERMISSION -> setPermission(redeemCode)
            JTab.Modify.LIST_TARGET -> sendMessage(JMessage.Code.Usages.TARGET)
            JTab.Modify.LIST_COMMAND -> sendMessage(JMessage.Code.Usages.COMMAND)
            JTab.Modify.Edit.REWARD -> openGUI(redeemCode, JTab.Modify.Edit.REWARD, placeHolder.sender)
            JTab.Modify.Edit.MESSAGE -> openGUI(redeemCode, JTab.Modify.Edit.MESSAGE, placeHolder.sender)
            JTab.Modify.Edit.SOUND -> openGUI(redeemCode, JTab.Modify.Edit.SOUND, placeHolder.sender)
            else -> false
        }
    }

    private fun modify(redeemTemplate: RedeemTemplate, property: String): Boolean {
        return when (property) {
            JTab.Modify.SYNC -> upsertTemplate(redeemTemplate)
            JTab.Modify.ENABLED -> toggleEnabledStatus(redeemTemplate)
            JTab.Modify.SET_PERMISSION -> setPermission(redeemTemplate)
            JTab.Modify.REQUIRED_PERMISSION -> setPermission(redeemTemplate)
            JTab.Modify.LIST_COMMAND -> {
                val commands = redeemTemplate.commands.withIndex().joinToString("\n")
                placeHolder.sender.sendMessage(commands)
                return true
            }

            JTab.Modify.Edit.REWARD -> openGUI(redeemTemplate, JTab.Modify.Edit.REWARD, placeHolder.sender)
            JTab.Modify.Edit.MESSAGE -> openGUI(redeemTemplate, JTab.Modify.Edit.MESSAGE, placeHolder.sender)
            JTab.Modify.Edit.SOUND -> openGUI(redeemTemplate, JTab.Modify.Edit.SOUND, placeHolder.sender)

            else -> false
        }
    }

    private fun modify(redeemCode: RedeemCode, property: String, value: String): Boolean {
        return when (property) {
            JTab.Modify.SET_TEMPLATE -> setTemplate(redeemCode, value)
            JTab.Modify.SET_DURATION -> adjustDuration(redeemCode, "0s", value, true)
            JTab.Modify.ADD_DURATION -> adjustDuration(redeemCode, redeemCode.duration, value, true)
            JTab.Modify.REMOVE_DURATION -> adjustDuration(redeemCode, redeemCode.duration, value, false)
            JTab.Modify.SET_COOLDOWN -> setCooldown(redeemCode, value)
            JTab.Modify.SET_REDEMPTION -> setRedemption(redeemCode, value)
            JTab.Modify.SET_PLAYER_LIMIT -> setPlayerLimit(redeemCode, value)
            JTab.Modify.SET_PERMISSION -> setPermission(redeemCode, value)
            JTab.Modify.SET_PIN -> setPin(redeemCode, value)
            else -> false
        }
    }

    private fun modify(redeemTemplate: RedeemTemplate, property: String, value: String): Boolean {
        return when (property) {
            JTab.Modify.SET_DURATION -> adjustDuration(redeemTemplate, "0s", value, true)
            JTab.Modify.ADD_DURATION -> adjustDuration(redeemTemplate, redeemTemplate.duration, value, true)
            JTab.Modify.REMOVE_DURATION -> adjustDuration(redeemTemplate, redeemTemplate.duration, value, false)
            JTab.Modify.SET_COOLDOWN -> setCooldown(redeemTemplate, value)
            JTab.Modify.SET_REDEMPTION -> setRedemption(redeemTemplate, value)
            JTab.Modify.SET_PLAYER_LIMIT -> setPlayerLimit(redeemTemplate, value)
            JTab.Modify.SET_PERMISSION -> setPermission(redeemTemplate, value)
            JTab.Modify.SET_PIN -> setPin(redeemTemplate, value)
            else -> false

        }
    }

    private fun modify(redeemCode: RedeemCode, property: String, value: MutableList<String>): Boolean {
        return when (property) {
            JTab.Modify.SET_TARGET -> setTarget(redeemCode, value)
            JTab.Modify.ADD_TARGET -> addTarget(redeemCode, value)
            JTab.Modify.REMOVE_TARGET -> removeTarget(redeemCode, value)
            JTab.Modify.SET_COMMAND -> setCommand(
                redeemCode, value[0].toIntOrNull() ?: return !sendMessage(JMessage.Code.Modify.INVALID_ID), value.drop(1).joinToString(" ")
            )

            JTab.Modify.ADD_COMMAND -> addCommand(redeemCode, value.drop(0).joinToString(" "))
            JTab.Modify.REMOVE_COMMAND -> removeCommand(
                redeemCode, value[0].toIntOrNull() ?: return if (value[0] == "*") removeAllCommand(redeemCode)
                else !sendMessage(JMessage.Code.Modify.INVALID_ID)
            )

            else -> false
        }
    }

    private fun modify(redeemTemplate: RedeemTemplate, property: String, value: MutableList<String>): Boolean {
        return when (property) {
            JTab.Modify.SET_COMMAND -> setCommand(
                redeemTemplate, value[0].toIntOrNull() ?: return !sendMessage(JMessage.Code.Modify.INVALID_ID), value.drop(1).joinToString(" ")
            )

            JTab.Modify.ADD_COMMAND -> addCommand(redeemTemplate, value.drop(0).joinToString(" "))
            JTab.Modify.REMOVE_COMMAND -> removeCommand(
                redeemTemplate, value[0].toIntOrNull() ?: return if (value[0] == "*") removeAllCommand(redeemTemplate)
                else return !sendMessage(JMessage.Code.Modify.INVALID_ID)
            )

            else -> false
        }
    }

    private fun upsertCode(redeemCode: RedeemCode): Boolean {
        redeemCode.modified = JService.getCurrentTime()
        val success = codeRepo.upsertCode(redeemCode)
        if (!success) {
            sendMessage(JMessage.Code.Modify.FAILED)
            JLogger(plugin).logModify(redeemCode.code, sender.name)
            return false
        }
        JLogger(plugin).logModify(redeemCode.code + " - ${redeemCode.template}", sender.name)
        return true
    }

    private fun upsetCodes(template: RedeemTemplate): Boolean {
        val redeemCode: List<RedeemCode> = codeRepo.getCodesByTemplate(template.name, true)
        if (redeemCode.isEmpty()) return false
        val codes: MutableList<String> = mutableListOf()
        redeemCode.forEach {
            codes.add(it.code)
            if (!codeRepo.templateToRedeemCode(it, template)) return false
            if (template.permissionRequired) it.permission = it.permission.replace("{code}", it.code.lowercase()) else it.permission = ""
            it.modified = JService.getCurrentTime()
        }

        placeHolder.totalCodes = codes.size
        placeHolder.code = codes.joinToString(" ")

        codeRepo.upsertCodes(redeemCode)
        return true
    }

    private fun upsertTemplate(template: RedeemTemplate): Boolean {
        val success = config.upsertTemplate(template)
        if (upsetCodes(template)) {
            JLogger(plugin).logModify(template.name + " (TEMPLATE)", sender.name)
            sendMessage(JMessage.Template.Modify.CODES_MODIFIED)
        }
        if (!success) {
            config.sendMsg(JMessage.Template.Modify.FAILED, placeHolder)
            return false
        }
        return true
    }

    private fun openGUI(redeemCode: RedeemCode, value: String, sender: CommandSender): Boolean {
        if (sender !is Player) return sendMessage(JMessage.Command.RESTRICTED_TO_PLAYERS)
        when (value) {
            JTab.Modify.Edit.REWARD -> {
                if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncRewards == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
                val rewardsHolder = RewardsHolder(sender, RedeemType.Code(redeemCode), 6, "Rewards GUI")
                sender.openInventory(rewardsHolder.inventory)
            }

            JTab.Modify.Edit.MESSAGE -> {
                if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncMessages == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
                val messageHolder = MessageHolder(plugin, sender, RedeemType.Code(redeemCode), 3, "Message GUI")
                sender.openInventory(messageHolder.inventory)

            }

            JTab.Modify.Edit.SOUND -> {
                if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncSound == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
                val soundHolder = SoundHolder(plugin, sender, RedeemType.Code(redeemCode), 6, "Sound GUI")
                sender.openInventory(soundHolder.inventory)
            }
        }
        return true
    }

    private fun openGUI(redeemTemplate: RedeemTemplate, value: String, sender: CommandSender): Boolean {
        if (sender !is Player) return sendMessage(JMessage.Command.RESTRICTED_TO_PLAYERS)
        when (value) {
            JTab.Modify.Edit.REWARD -> {
                val rewardsHolder = RewardsHolder(sender, RedeemType.Template(redeemTemplate), 6, "Rewards GUI - ${redeemTemplate.name}")
                sender.openInventory(rewardsHolder.inventory)
            }

            JTab.Modify.Edit.MESSAGE -> {
                val messageHolder = MessageHolder(plugin, sender, RedeemType.Template(redeemTemplate), 3, "Message GUI - ${redeemTemplate.name}")
                sender.openInventory(messageHolder.inventory)
            }

            JTab.Modify.Edit.SOUND -> {
                val soundHolder = SoundHolder(plugin, sender, RedeemType.Template(redeemTemplate), 6, "Sound GUI - $redeemTemplate.name")
                sender.openInventory(soundHolder.inventory)
            }
        }
        return true
    }

    private fun setRedemption(redeemCode: RedeemCode, value: String): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncRedemption == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        placeHolder.redemptionLimit = value
        redeemCode.redemption = value.toIntOrNull() ?: return !sendMessage(JMessage.Code.Modify.INVALID_VALUE)
        sendMessage(JMessage.Code.Modify.SET_REDEMPTION)
        return upsertCode(redeemCode)
    }

    private fun setRedemption(redeemTemplate: RedeemTemplate, value: String): Boolean {
        placeHolder.redemptionLimit = value
        redeemTemplate.redemption = value.toIntOrNull() ?: return !sendMessage(JMessage.Template.Modify.INVALID_VALUE)
        sendMessage(JMessage.Template.Modify.SET_REDEMPTION)
        return upsertTemplate(redeemTemplate)
    }

    private fun setPlayerLimit(redeemCode: RedeemCode, value: String): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncPlayerLimit == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        placeHolder.playerLimit = value
        redeemCode.playerLimit = value.toIntOrNull() ?: return !sendMessage(JMessage.Code.Modify.INVALID_VALUE)
        sendMessage(JMessage.Code.Modify.SET_PLAYER_LIMIT)
        return upsertCode(redeemCode)
    }

    private fun setPlayerLimit(redeemTemplate: RedeemTemplate, value: String): Boolean {
        placeHolder.playerLimit = value
        redeemTemplate.playerLimit = value.toIntOrNull() ?: return !sendMessage(JMessage.Template.Modify.INVALID_VALUE)
        sendMessage(JMessage.Template.Modify.SET_PLAYER_LIMIT)
        return upsertTemplate(redeemTemplate)
    }

    private fun setPin(redeemCode: RedeemCode, value: String): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncPin == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        placeHolder.pin = value
        redeemCode.pin = value.toIntOrNull() ?: return !sendMessage(JMessage.Code.Modify.INVALID_VALUE)
        sendMessage(JMessage.Code.Modify.SET_PIN)
        return upsertCode(redeemCode)
    }

    private fun setPin(redeemTemplate: RedeemTemplate, value: String): Boolean {
        placeHolder.pin = value
        redeemTemplate.pin = value.toIntOrNull() ?: return !sendMessage(JMessage.Template.Modify.INVALID_VALUE)
        sendMessage(JMessage.Template.Modify.SET_PIN)
        return upsertTemplate(redeemTemplate)
    }

    private fun adjustDuration(redeemCode: RedeemCode, existingDuration: String, duration: String, isAdding: Boolean): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncDuration == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        if (!JService.isDurationValid(duration)) return sendMessage(JMessage.Code.Modify.INVALID_VALUE)
        redeemCode.duration = JService.adjustDuration(existingDuration, duration, isAdding)
        placeHolder.duration = redeemCode.duration
        sendMessage(JMessage.Code.Modify.SET_DURATION)
        return upsertCode(redeemCode)
    }

    private fun adjustDuration(redeemTemplate: RedeemTemplate, existingDuration: String, duration: String, isAdding: Boolean): Boolean {
        if (!JService.isDurationValid(duration)) return sendMessage(JMessage.Template.Modify.INVALID_VALUE)
        redeemTemplate.duration = JService.adjustDuration(existingDuration, duration, isAdding)
        placeHolder.duration = redeemTemplate.duration
        sendMessage(JMessage.Template.Modify.SET_DURATION)
        return upsertTemplate(redeemTemplate)
    }

    private fun setCooldown(redeemCode: RedeemCode, duration: String): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncCooldown == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        placeHolder.cooldown = duration
        if (!JService.isDurationValid(duration)) return !sendMessage(JMessage.Code.Modify.INVALID_VALUE)
        redeemCode.cooldown = duration
        if (duration.isBlank()) redeemCode.cooldown = "0s"
        sendMessage(JMessage.Code.Modify.SET_COOLDOWN)
        return upsertCode(redeemCode)
    }

    private fun setCooldown(redeemTemplate: RedeemTemplate, duration: String): Boolean {
        placeHolder.cooldown = duration
        if (!JService.isDurationValid(duration)) return !sendMessage(JMessage.Template.Modify.INVALID_VALUE)
        redeemTemplate.cooldown = duration
        if (duration.isBlank()) redeemTemplate.cooldown = "0s"
        sendMessage(JMessage.Template.Modify.SET_COOLDOWN)
        return upsertTemplate(redeemTemplate)
    }

    private fun setPermission(redeemCode: RedeemCode, value: String = ""): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncPermission == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)

        //Set Custom Permission
        if (value.isNotBlank() && value != "false") {
            redeemCode.permission = value.replace("{code}", redeemCode.code)
            placeHolder.permission = redeemCode.permission
            sendMessage(JMessage.Code.Modify.SET_PERMISSION)
            return upsertCode(redeemCode)
        }

        //Toggle Permission
        if (redeemCode.permission.isNotBlank() && redeemCode.permission != "false") {
            redeemCode.permission = ""
            placeHolder.permission = config.getMessage(JMessage.Code.Placeholder.DISABLED, placeHolder)
            sendMessage(JMessage.Code.Modify.SET_PERMISSION)
            return upsertCode(redeemCode)
        }

        val codePermission = config.getTemplateValue(redeemCode.template, JTemplate.PERMISSION_VALUE.property)
        redeemCode.permission = codePermission.replace("{code}", redeemCode.code.lowercase())
        placeHolder.permission = redeemCode.permission
        sendMessage(JMessage.Code.Modify.SET_PERMISSION)
        return upsertCode(redeemCode)
    }

    private fun setPermission(redeemTemplate: RedeemTemplate, value: String = ""): Boolean {
        //Set Custom Permission
        if (value.isNotBlank() && value != "false") {
            redeemTemplate.permissionValue = value
            redeemTemplate.permissionRequired = true
            placeHolder.permission = redeemTemplate.permissionValue
            sendMessage(JMessage.Template.Modify.SET_PERMISSION)
            return upsertTemplate(redeemTemplate)
        }

        //Toggle Permission
        redeemTemplate.permissionRequired = !redeemTemplate.permissionRequired
        placeHolder.permission = config.getMessage(JMessage.Code.Placeholder.DISABLED, placeHolder)
        sendMessage(JMessage.Template.Modify.SET_PERMISSION)
        return upsertTemplate(redeemTemplate)
    }

    private fun setCommand(redeemCode: RedeemCode, id: Int, command: String): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncCommands == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        placeHolder.command = command
        placeHolder.commandId = id.toString()
        if (command.isEmpty()) return !sendMessage(JMessage.Code.Modify.INVALID_VALUE)
        if (id >= redeemCode.commands.size) return !sendMessage(JMessage.Code.Modify.INVALID_ID)
        redeemCode.commands[id] = command
        sendMessage(JMessage.Code.Modify.SET_COMMAND)
        return upsertCode(redeemCode)
    }

    private fun setCommand(redeemTemplate: RedeemTemplate, id: Int, command: String): Boolean {
        placeHolder.commandId = id.toString()
        placeHolder.command = command
        if (command.isBlank()) return config.sendMsg(JMessage.Template.Modify.INVALID_VALUE, placeHolder) != Unit
        if (id > redeemTemplate.commands.size) return config.sendMsg(JMessage.Template.Modify.INVALID_VALUE, placeHolder) != Unit
        redeemTemplate.commands[id] = command
        sendMessage(JMessage.Template.Modify.SET_COMMAND)
        return upsertTemplate(redeemTemplate)
    }

    private fun addCommand(redeemCode: RedeemCode, command: String): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncCommands == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        placeHolder.command = command
        if (command.isBlank()) return !sendMessage(JMessage.Code.Modify.INVALID_VALUE)
        redeemCode.commands.add(command)
        placeHolder.commandId = (redeemCode.commands.size - 1).toString()
        sendMessage(JMessage.Code.Modify.ADD_COMMAND)
        return upsertCode(redeemCode)
    }

    private fun addCommand(redeemTemplate: RedeemTemplate, command: String): Boolean {
        placeHolder.command = command
        if (command.isBlank()) return config.sendMsg(JMessage.Template.Modify.INVALID_VALUE, placeHolder) != Unit
        redeemTemplate.commands.add(command)
        placeHolder.commandId = (redeemTemplate.commands.size - 1).toString()
        sendMessage(JMessage.Template.Modify.ADD_COMMAND)
        return upsertTemplate(redeemTemplate)
    }

    private fun removeCommand(redeemCode: RedeemCode, id: Int): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncCommands == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        placeHolder.commandId = id.toString()
        if (redeemCode.commands.isEmpty() || id >= redeemCode.commands.size || id < 0) return !sendMessage(JMessage.Code.Modify.INVALID_ID)
        placeHolder.command = redeemCode.commands[id]
        redeemCode.commands.removeAt(id)
        sendMessage(JMessage.Code.Modify.REMOVE_COMMAND)
        return upsertCode(redeemCode)
    }

    private fun removeCommand(redeemTemplate: RedeemTemplate, id: Int): Boolean {
        placeHolder.commandId = id.toString()
        if (redeemTemplate.commands.isEmpty() || id >= redeemTemplate.commands.size || id < 0) return config.sendMsg(
            JMessage.Template.Modify.INVALID_ID, placeHolder
        ) != Unit
        placeHolder.command = redeemTemplate.commands[id]
        redeemTemplate.commands.removeAt(id)
        sendMessage(JMessage.Template.Modify.REMOVE_COMMAND)
        return upsertTemplate(redeemTemplate)
    }

    private fun removeAllCommand(redeemCode: RedeemCode): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncCommands == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        redeemCode.commands.clear()
        placeHolder.command = ""
        sendMessage(JMessage.Code.Modify.REMOVE_ALL_COMMAND)
        return upsertCode(redeemCode)
    }

    private fun removeAllCommand(redeemTemplate: RedeemTemplate): Boolean {
        redeemTemplate.commands.clear()
        placeHolder.command = ""
        sendMessage(JMessage.Template.Modify.REMOVE_ALL_COMMAND)
        return upsertTemplate(redeemTemplate)
    }

    private fun toggleEnabledStatus(redeemCode: RedeemCode): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncEnabledStatus == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        redeemCode.enabledStatus = !redeemCode.enabledStatus
        placeHolder.status = redeemCode.enabledStatus.toString()
        sendMessage(JMessage.Code.Modify.ENABLED_STATUS)
        return upsertCode(redeemCode)
    }

    private fun toggleEnabledStatus(redeemTemplate: RedeemTemplate): Boolean {
        redeemTemplate.defaultEnabledStatus = !redeemTemplate.defaultEnabledStatus
        placeHolder.status = redeemTemplate.defaultEnabledStatus.toString()
        sendMessage(JMessage.Template.Modify.SET_DEFAULT_ENABLED_STATUS)
        return upsertTemplate(redeemTemplate)
    }

    private fun toggleTemplateSyncStatus(redeemCode: RedeemCode): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncLockedStatus == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        placeHolder.templateSync = redeemCode.sync.toString()
        redeemCode.sync = !redeemCode.sync
        if (redeemCode.template.isBlank()) return !sendMessage(JMessage.Template.NOT_FOUND)
        sendMessage(JMessage.Code.Modify.SYNC_STATUS)
        return upsertCode(redeemCode)
    }

    private fun setTemplate(redeemCode: RedeemCode, template: String): Boolean {
        redeemCode.template = template
        placeHolder.template = template
        val templateState = config.getTemplate(redeemCode.template) ?: return sendMessage(JMessage.Template.NOT_FOUND)
        sendMessage(JMessage.Code.Modify.SET_TEMPLATE)
        if (codeRepo.templateToRedeemCode(redeemCode, templateState)) sendMessage(JMessage.Code.Modify.SYNC)
        return upsertCode(redeemCode)
    }

    private fun addTarget(redeemCode: RedeemCode, targetList: MutableList<String>): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncTarget == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        if (targetList.isEmpty()) return !sendMessage(JMessage.Code.Modify.INVALID_VALUE)
        redeemCode.target.addAll(targetList)
        placeHolder.target = targetList.joinToString(", ")
        sendMessage(JMessage.Code.Modify.ADD_TARGET)
        return upsertCode(redeemCode)
    }

    private fun removeTarget(redeemCode: RedeemCode, targetList: MutableList<String>): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncTarget == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        if (targetList.isEmpty()) return !sendMessage(JMessage.Code.Modify.INVALID_VALUE)
        redeemCode.target.removeAll(targetList)
        placeHolder.target = targetList.joinToString(", ")
        sendMessage(JMessage.Code.Modify.REMOVE_TARGET)
        return upsertCode(redeemCode)
    }

    private fun setTarget(redeemCode: RedeemCode, targetList: MutableList<String>): Boolean {
        if (redeemCode.sync && config.getTemplate(redeemCode.template)?.syncTarget == true) return sendMessage(JMessage.Code.Modify.SYNC_LOCKED)
        redeemCode.target.clear()
        redeemCode.target.addAll(targetList)
        placeHolder.target = targetList.joinToString(", ")
        sendMessage(JMessage.Code.Modify.SET_TARGET)
        return upsertCode(redeemCode)
    }
}


