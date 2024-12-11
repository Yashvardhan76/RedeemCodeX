package me.justlime.redeemX.data.repository

import me.justlime.redeemX.data.config.ConfigDao
import me.justlime.redeemX.data.config.JFiles
import me.justlime.redeemX.data.models.RedeemTemplate
import me.justlime.redeemX.state.RedeemCodeState

/**
 * This repository class is responsible for managing configurations. Such as config.yml, messages.yml, and templates.yml.
 */
class ConfigRepository(private val jConfig: ConfigDao) {


    fun getConfigValue(key: String): String{
        return jConfig.getString(key, JFiles.CONFIG, false) ?: ""
    }

    fun setConfigValue(key: String): Boolean{
        try {
            jConfig.upsertConfig(JFiles.CONFIG,key,"")
            return true
        }catch (e:Exception){
            return false
        }
    }

    /**Get a Simple Text Message**/
    fun getMessage(message: String): String {
        return jConfig.getMessage(message)
    }

    /**Get a Colored Text Message with placeholders**/
    fun getFormattedMessage(message: String, placeholders: Map<String, String>): String {
        return jConfig.getFormattedMessage(message, placeholders)
    }

    fun sendMsg(key: String, state: RedeemCodeState){
        state.sender.sendMessage(jConfig.getFormattedMessage(key, state.toPlaceholdersMap()))
    }
    fun getTemplate(template: String = "default"): RedeemTemplate? {
        return try {
            jConfig.getTemplate(template)
        } catch (e:Exception){
            null
        }
    }

    fun getTemplateValue(template: String,property: String): String{
        return jConfig.getString("$template.$property",JFiles.TEMPLATE,false) ?: ""
    }

    fun getEntireTemplates(): List<RedeemTemplate> {
        return jConfig.getEntireTemplates()
    }

    fun createTemplate(template: RedeemTemplate): Boolean {
        if(getTemplate(template.name) == null){
            jConfig.upsertTemplate(template = template)
            return true
        }
        return false
    }

    fun modifyTemplate(template: RedeemTemplate): Boolean {
        if(getTemplate(template.name) == null) return false
        return jConfig.upsertTemplate(template = template)
    }

    fun modifyTemplates(template: List<RedeemTemplate>){
        template.forEach { modifyTemplate(it) }
    }

    fun deleteTemplate(name: String): Boolean {
        return jConfig.deleteTemplate(name)
    }

    fun deleteEntireTemplates(): Boolean {
        return jConfig.deleteEntireTemplates()
    }

    fun reloadConfig(): Boolean {
        return jConfig.reloadAllConfigs()
    }

}
