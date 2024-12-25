package me.justlime.redeemX.api

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.DeleteSubCommand
import me.justlime.redeemX.commands.subcommands.GenerateSubCommand
import me.justlime.redeemX.commands.subcommands.ModifySubCommand
import me.justlime.redeemX.commands.subcommands.ModifyTemplateSubCommand
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.enums.JTab
import org.bukkit.command.CommandSender

@Suppress("unused")
object RedeemXAPI {
    private lateinit var plugin: RedeemX

    fun initialize(pluginInstance: RedeemX) {
        plugin = pluginInstance
    }

    fun generateCode(sender: CommandSender, uniqueName: String): Boolean {
        val args = mutableListOf(JTab.GeneralActions.Gen.value, uniqueName)
        val gen = GenerateSubCommand(plugin)
        gen.execute(sender, args)
        return gen.codeList.isNotEmpty()
    }

    fun generateCodes(sender: CommandSender, digit: Int, amount: String = "1"): List<String> {
        val args = mutableListOf(JTab.GeneralActions.Gen.value, digit.toString(), amount)
        val gen = GenerateSubCommand(plugin)
        gen.execute(sender, args)
        return gen.codeList
    }

    fun generateCodes(sender: CommandSender, template: String, amount: String = "1"): List<String> {
        val args = mutableListOf(JTab.GeneralActions.Gen.value, template, amount)
        val gen = GenerateSubCommand(plugin)
        gen.execute(sender, args)
        return gen.codeList
    }

    fun modifyCodes(sender: CommandSender, code: String, property: JTab.Modify, value: String = ""): List<String> {
        val modify = ModifySubCommand(plugin)
        val args = if (value.isNotBlank() || value.isNotBlank()) mutableListOf(JTab.GeneralActions.Modify.value,JTab.Type.Code.value, code, property.value)
        else mutableListOf(JTab.GeneralActions.Modify.value, code, property.value, value)
        modify.execute(sender, args)
        return modify.codeList
    }
    //TODO Improve Required
    fun modifyTemplate(sender: CommandSender, template: String, property: JTab.Modify, value: String = ""): List<String>{
        val templateSC = ModifyTemplateSubCommand(plugin)
        val args = if (value.isNotBlank() || value.isNotBlank()) mutableListOf(JTab.GeneralActions.Modify.value,JTab.Type.Template.value ,template, property.value)
        else mutableListOf(JTab.GeneralActions.Modify.value, template, property.value, value)
        templateSC.execute(sender, args)
        return templateSC.codeList
    }

    fun deleteCode(sender: CommandSender, code: String): String {
        val delete = DeleteSubCommand(plugin)
        val args = mutableListOf(JTab.GeneralActions.Delete.value,JTab.Type.Code.value, code)
        delete.execute(sender, args)
        return delete.codeList.firstOrNull() ?: ""

    }

    fun deleteCodes(sender: CommandSender,codes: List<String>): List<String>{
        val delete = DeleteSubCommand(plugin)
        val args: MutableList<String> = mutableListOf(JTab.GeneralActions.Delete.value,JTab.Type.Code.value)
        args.addAll(codes)
        delete.execute(sender, args)
        return delete.codeList

    }

    fun deleteAllCode(sender: CommandSender){
        val delete = DeleteSubCommand(plugin)
        val args = mutableListOf(JTab.GeneralActions.Delete.value, JTab.Delete.All.value)
        delete.execute(sender, args)

    }

    fun getPlugin(): RedeemX {
        return plugin
    }

    fun getCodes(): List<String> {
        return RedeemCodeRepository(plugin).getCachedCode()
    }

    fun getTemplates(): List<String> {
        val templates = ConfigRepository(plugin).getAllTemplates()
        return templates.map { it.name }
    }

    fun isInitialized() = ::plugin.isInitialized
}