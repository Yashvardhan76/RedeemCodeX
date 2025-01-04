package me.justlime.redeemcodex.data.repository

import me.justlime.redeemcodex.RedeemCodeX
import me.justlime.redeemcodex.data.config.ConfigDao
import me.justlime.redeemcodex.data.config.ConfigImpl
import me.justlime.redeemcodex.enums.JConfig
import me.justlime.redeemcodex.enums.JFiles
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.models.RedeemTemplate

/**
 * This repository class is responsible for managing configurations. Such as config.yml, messages_en.yml, and templates.yml.
 */
class ConfigRepository(val plugin: RedeemCodeX) {

    private val config: ConfigDao = ConfigImpl(plugin)
    fun getConfigValue(key: String): String{
        return config.getString(key, JFiles.CONFIG, false) ?: ""
    }

    fun getCodeLengthRange(placeHolder: CodePlaceHolder): Pair<Int, Int> {
        val minLength = getConfigValue(JConfig.Code.MINIMUM_DIGIT).toIntOrNull() ?: 3
        val maxLength = getConfigValue(JConfig.Code.MAXIMUM_DIGIT).toIntOrNull() ?: 25
        placeHolder.minLength = minLength.toString()
        placeHolder.maxLength = maxLength.toString()
        return minLength to maxLength
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

    fun sendTemplateMsg(template: String, placeHolder: CodePlaceHolder){
        config.sendTemplateMsg(template, placeHolder)
    }

    fun loadDefaultTemplateValues(template: String): RedeemTemplate {
        return config.loadDefaultTemplateValues(template)
    }

    fun getTemplate(template: String = "DEFAULT"): RedeemTemplate? {
        return try {
            config.getTemplate(template)
        } catch (e:Exception){
            null
        }
    }

    fun getTemplateValue(template: String,property: String): String{
        return config.getString("$template.$property", JFiles.TEMPLATE,false) ?: ""
    }

    fun getAllTemplates(): List<RedeemTemplate> {
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

    fun deleteAllTemplates(): Boolean {
        return config.deleteAllTemplates()
    }

    fun reloadConfig(jFiles: JFiles): Boolean {
        return config.reloadConfig(jFiles)
    }

}
