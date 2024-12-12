package me.justlime.redeemX.data.config

import me.justlime.redeemX.models.CodePlaceHolder

interface ConfigDao {
    fun getString(path: String, configFile: JFiles, applyColor: Boolean): String?
    fun getMessage(message: String, placeholders: CodePlaceHolder): String
    fun getFormattedMessage(message: String, placeholders: CodePlaceHolder): String
    fun sendMsg(key: String, placeHolder: CodePlaceHolder)
    fun getTemplate(template: String): me.justlime.redeemX.models.RedeemTemplate
    fun getEntireTemplates(): List<me.justlime.redeemX.models.RedeemTemplate>
    fun upsertTemplate(template: me.justlime.redeemX.models.RedeemTemplate): Boolean
    fun deleteTemplate(name: String): Boolean
    fun deleteEntireTemplates(): Boolean

    fun upsertConfig(configFile: JFiles, path: String, value: String): Boolean
    fun saveAllConfigs(): Boolean
    fun reloadConfig(configFile: JFiles): Boolean
    fun reloadAllConfigs(): Boolean
}
