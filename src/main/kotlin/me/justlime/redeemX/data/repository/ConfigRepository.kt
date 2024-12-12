package me.justlime.redeemX.data.repository

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.ConfigDao
import me.justlime.redeemX.data.config.ConfigImpl
import me.justlime.redeemX.data.config.JFiles
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemTemplate

/**
 * This repository class is responsible for managing configurations. Such as config.yml, messages.yml, and templates.yml.
 */
class ConfigRepository(private val plugin: RedeemX) {

    private val config: ConfigDao = ConfigImpl(plugin)
    fun getConfigValue(key: String): String{
        return config.getString(key, JFiles.CONFIG, false) ?: ""
    }

    fun setConfigValue(key: String): Boolean{
        try {
            config.upsertConfig(JFiles.CONFIG,key,"")
            return true
        }catch (e:Exception){
            return false
        }
    }

    /**Get a Simple Text Message**/
    fun getMessage(message: String,placeHolder: CodePlaceHolder): String {
        return config.getMessage(message, placeHolder)
    }

    /**Get a Colored Text Message with placeholders**/
    fun getFormattedMessage(message: String, placeHolder: CodePlaceHolder): String {
        return config.getFormattedMessage(message, placeHolder)
    }

    fun sendMsg(key: String, placeHolder: CodePlaceHolder){
        config.sendMsg(key, placeHolder)
    }
    fun getTemplate(template: String = "default"): RedeemTemplate? {
        return try {
            config.getTemplate(template)
        } catch (e:Exception){
            null
        }
    }

    fun getTemplateValue(template: String,property: String): String{
        return config.getString("$template.$property",JFiles.TEMPLATE,false) ?: ""
    }

    fun getEntireTemplates(): List<RedeemTemplate> {
        return config.getEntireTemplates()
    }

    fun createTemplate(template: RedeemTemplate): Boolean {
        if(getTemplate(template.name) == null){
            config.upsertTemplate(template = template)
            return true
        }
        return false
    }

    fun modifyTemplate(template: RedeemTemplate): Boolean {
        if(getTemplate(template.name) == null) return false
        return config.upsertTemplate(template = template)
    }

    fun modifyTemplates(template: List<RedeemTemplate>){
        template.forEach { modifyTemplate(it) }
    }

    fun deleteTemplate(name: String): Boolean {
        return config.deleteTemplate(name)
    }

    fun deleteEntireTemplates(): Boolean {
        return config.deleteEntireTemplates()
    }

    fun reloadConfig(): Boolean {
        return config.reloadAllConfigs()
    }

}
