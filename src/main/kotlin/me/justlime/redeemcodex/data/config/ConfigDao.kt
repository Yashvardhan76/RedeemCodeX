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


package me.justlime.redeemcodex.data.config

import me.justlime.redeemcodex.enums.JFiles
import me.justlime.redeemcodex.models.CodePlaceHolder
import me.justlime.redeemcodex.models.RedeemTemplate

interface ConfigDao {
    fun getString(path: String, configFile: JFiles, applyColor: Boolean): String?
    fun getMessage(key: String): String
    fun getMessage(key: String, placeholders: CodePlaceHolder): String
    fun getTemplateMessage(template: String): String
    fun getTemplateMessage(template: String, message: String): String
    fun getFormattedMessage(message: String, placeholders: CodePlaceHolder): String
    fun getFormattedTemplateMessage(message: String, placeholders: CodePlaceHolder): String
    fun sendMsg(key: String, placeHolder: CodePlaceHolder)
    fun loadDefaultTemplateValues(template: String): RedeemTemplate
    fun getTemplate(template: String): RedeemTemplate
    fun getEntireTemplates(): List<RedeemTemplate>
    fun upsertTemplate(template: RedeemTemplate): Boolean
    fun deleteTemplate(name: String): Boolean
    fun deleteAllTemplates(): Boolean

    fun upsertConfig(configFile: JFiles, path: String, value: String): Boolean
    fun saveAllConfigs(): Boolean
    fun reloadConfig(configFile: JFiles): Boolean
    fun reloadAllConfigs(): Boolean
}
