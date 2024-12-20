package me.justlime.redeemX.api

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.commands.subcommands.GenerateSubCommand
import me.justlime.redeemX.commands.subcommands.ModifySubCommand
import me.justlime.redeemX.enums.Tab
import org.bukkit.command.CommandSender

@Suppress("unused")
object RedeemXAPI {
    private lateinit var plugin: RedeemX

    fun initialize(pluginInstance: RedeemX) {
        plugin = pluginInstance
    }

    fun generateCode(sender: CommandSender, uniqueName: String): Boolean {
        val args = mutableListOf(Tab.GeneralActions.Gen.value, uniqueName)
        val gen = GenerateSubCommand(plugin)
        gen.execute(sender, args)
        return gen.codeList.isNotEmpty()
    }

    fun generateCodes(sender: CommandSender, digit: Int, amount: String = "1"): List<String> {
        val args = mutableListOf(Tab.GeneralActions.Gen.value, digit.toString(), amount)
        val gen = GenerateSubCommand(plugin)
        gen.execute(sender, args)
        return gen.codeList
    }

    fun generateCodes(sender: CommandSender, template: String, amount: String = "1"): List<String> {
        val args = mutableListOf(Tab.GeneralActions.Gen.value, template, amount)
        val gen = GenerateSubCommand(plugin)
        gen.execute(sender, args)
        return gen.codeList
    }

    fun modifyCodes(sender: CommandSender, code: String, property: Tab.Modify, value: String): List<String> {
        val modify = ModifySubCommand(plugin)
        val args = if (value.isNotBlank() || value.isNotBlank()) mutableListOf(Tab.GeneralActions.Modify.value, code, property.value)
        else mutableListOf(Tab.GeneralActions.Modify.value, code, property.value, value)
        modify.execute(sender, args)
        return modify.codeList
    }

    fun deleteCodes(sender: CommandSender, args: MutableList<String>): List<String> {
        TODO()
    }

    fun getPlugin(): RedeemX {
        return plugin
    }

    fun isInitialized() = ::plugin.isInitialized
}