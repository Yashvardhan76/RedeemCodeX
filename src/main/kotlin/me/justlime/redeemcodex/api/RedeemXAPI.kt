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


package me.justlime.redeemcodex.api

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.commands.subcommands.DeleteSubCommand
import me.justlime.redeemcodex.commands.subcommands.GenerateSubCommand
import me.justlime.redeemcodex.commands.subcommands.ModifySubCommand
import me.justlime.redeemcodex.commands.subcommands.UsageSubCommand
import me.justlime.redeemcodex.data.repository.ConfigRepository
import me.justlime.redeemcodex.data.repository.RedeemCodeRepository
import me.justlime.redeemcodex.enums.JTab
import me.justlime.redeemcodex.models.CodePlaceHolder
import org.bukkit.command.CommandSender

@Suppress("unused")
object RedeemXAPI {
    private lateinit var plugin: RedeemCodeX
    lateinit var placeHolder: CodePlaceHolder
    val sender by lazy { plugin.server.consoleSender }
    fun initialize(pluginInstance: RedeemCodeX) {
        plugin = pluginInstance
        placeHolder = CodePlaceHolder(sender)
        plugin.logger.info("RedeemXAPI Initialized")
    }

    /**
     *Generate Custom Code
     *
     * @param uniqueName The unique name of the code to generate.
     * @return List of Code if Success else empty List
     **/
    fun generateCode(uniqueName: String, template: String = "DEFAULT"): List<String> {
        val args = mutableListOf(JTab.GeneralActions.Gen.value, JTab.Type.CODE, uniqueName, template)
        val gen = GenerateSubCommand(plugin)
        gen.execute(sender, args)
        placeHolder = gen.placeHolder
        return gen.jList
    }

    /**
     *      Generate Numeric Codes
     *
     * @param digit The number of digits to generate for each code.
     * @param amount The number of codes to generate.
     * @return List of Code if Success else empty List
     */
    fun generateCode(digit: Int, amount: Int = 1, template: String = "DEFAULT"): List<String> {
        val args = mutableListOf(JTab.GeneralActions.Gen.value, JTab.Type.CODE, digit.toString(), template, amount.toString())
        val gen = GenerateSubCommand(plugin)
        gen.execute(sender, args)
        placeHolder = gen.placeHolder
        return gen.jList
    }

    fun generateTemplate(template: String): List<String> {
        val args = mutableListOf(JTab.GeneralActions.Gen.value, JTab.Type.TEMPLATE, template)
        val gen = GenerateSubCommand(plugin)
        gen.execute(sender, args)
        placeHolder = gen.placeHolder
        return gen.jList
    }

    fun modifyCode(code: String, property: String, value: String = "",sender: CommandSender? = null): List<String> {

        val options = mutableListOf(JTab.Modify.ENABLED, JTab.Modify.SYNC)
        val optionsWithValue = mutableListOf(
            JTab.Modify.SET_REDEMPTION,
            JTab.Modify.SET_PLAYER_LIMIT,
            JTab.Modify.SET_COMMAND,
            JTab.Modify.ADD_COMMAND,
            JTab.Modify.REMOVE_COMMAND,
            JTab.Modify.SET_DURATION,
            JTab.Modify.ADD_DURATION,
            JTab.Modify.REMOVE_DURATION,
            JTab.Modify.SET_PERMISSION,
            JTab.Modify.SET_PIN,
            JTab.Modify.SET_TARGET,
            JTab.Modify.ADD_TARGET,
            JTab.Modify.REMOVE_TARGET,
            JTab.Modify.SET_COOLDOWN,
            JTab.Modify.SET_TEMPLATE,
            JTab.Modify.EDIT
        )
        val modify = ModifySubCommand(plugin)
        val args = if (value.isNotBlank() && value.isNotEmpty() && property in optionsWithValue) mutableListOf(
            JTab.GeneralActions.Modify.value, JTab.Type.CODE, code, property, value
        )
        else if (property in options) mutableListOf(JTab.GeneralActions.Modify.value, JTab.Type.CODE, code, property)
        else return emptyList()
        modify.execute(sender ?: plugin.server.consoleSender, args)
        placeHolder = modify.placeHolder
        return modify.jList
    }

    fun modifyTemplate(template: String, property: String, value: String = "",sender: CommandSender? = null): List<String> {
        val options = mutableListOf(JTab.Modify.ENABLED, JTab.Modify.SYNC)
        val optionsWithValue = mutableListOf(
            JTab.Modify.SET_REDEMPTION,
            JTab.Modify.SET_PLAYER_LIMIT,
            JTab.Modify.SET_COMMAND,
            JTab.Modify.ADD_COMMAND,
            JTab.Modify.REMOVE_COMMAND,
            JTab.Modify.SET_DURATION,
            JTab.Modify.ADD_DURATION,
            JTab.Modify.REMOVE_DURATION,
            JTab.Modify.SET_PERMISSION,
            JTab.Modify.REQUIRED_PERMISSION,
            JTab.Modify.SET_PIN,
            JTab.Modify.SET_COOLDOWN,
            JTab.Modify.SET_TEMPLATE,
            JTab.Modify.EDIT
        )
        val modify = ModifySubCommand(plugin)
        val args = if (value.isNotBlank() && value.isNotEmpty() && property in optionsWithValue) mutableListOf(
            JTab.GeneralActions.Modify.value, JTab.Type.TEMPLATE, template, property, value
        )
        else if (property in options) mutableListOf(JTab.GeneralActions.Modify.value, JTab.Type.TEMPLATE, template, property)
        else return emptyList()
        modify.execute( sender?: plugin.server.consoleSender, args)
        placeHolder = modify.placeHolder
        return modify.jList
    }

    fun deleteCode(code: String): String {
        val delete = DeleteSubCommand(plugin)
        val args = mutableListOf(JTab.GeneralActions.Delete.value, JTab.Type.CODE, code)
        delete.execute(sender, args)
        placeHolder = delete.placeHolder
        return delete.jList.firstOrNull() ?: ""

    }

    fun deleteCodes(codes: List<String>): List<String> {
        val delete = DeleteSubCommand(plugin)
        val args: MutableList<String> = mutableListOf(JTab.GeneralActions.Delete.value, JTab.Type.CODE)
        args.addAll(codes)
        delete.execute(sender, args)
        placeHolder = delete.placeHolder
        return delete.jList

    }

    fun deleteAllCode() {
        val delete = DeleteSubCommand(plugin)
        val args = mutableListOf(JTab.GeneralActions.Delete.value, JTab.Delete.All.value)
        delete.execute(sender, args)
        placeHolder = delete.placeHolder
    }

    fun deleteTemplate(template: String): List<String> {
        val delete = DeleteSubCommand(plugin)
        val args = mutableListOf(JTab.GeneralActions.Delete.value, JTab.Type.TEMPLATE, template)
        delete.execute(sender, args)
        placeHolder = delete.placeHolder
        return delete.jList
    }

    fun usageCode(code: String): CodePlaceHolder {
        val usage = UsageSubCommand(plugin)
        val args = mutableListOf(JTab.GeneralActions.Usage.value, JTab.Type.CODE, code)
        usage.execute(sender, args)
        placeHolder = usage.placeHolder
        return usage.placeHolder
    }

    fun usageTemplate(template: String): CodePlaceHolder {
        val usage = UsageSubCommand(plugin)
        val args = mutableListOf(JTab.GeneralActions.Usage.value, JTab.Type.TEMPLATE, template)
        usage.execute(sender, args)
        placeHolder = usage.placeHolder
        return usage.placeHolder
    }

    fun getPlugin(): RedeemCodeX = plugin

    fun getCodes(): List<String> = RedeemCodeRepository(plugin).getCachedCode()

    fun getTemplates(): List<String> = ConfigRepository(plugin).getAllTemplates().map { it.name }

    fun isInitialized() = ::plugin.isInitialized
}