/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *  This file handles the core logic for redeeming codes and managing associated data.
 *
 */


package me.justlime.redeemcodex.enums

sealed interface JTab {

    enum class GeneralActions(val value: String) {
        Gen("gen"),

        //GUI("gui"), //TODO

        Modify("modify"),


        Delete("delete"),

        Info("info"),

        Reload("reload"),

        Preview("preview"),

        Usage("usage"),

        Renew("renew"),

        Help("help")

        //GUI("gui"), //TODO

    }

    sealed interface Type {
        companion object {
            const val CODE = "code"
            const val TEMPLATE = "template"
        }
    }

    sealed interface Generate {
        companion object {
            const val CUSTOM = "custom"
            const val AMOUNT = "amount"
            const val DIGIT = "digit"
            const val TEMPLATE_NAME = "template-name"
        }
    }

    sealed interface Modify {
        companion object {
            const val ENABLED = "enabled"
            const val SYNC = "sync"
            const val SET_REDEMPTION = "redemption"
            const val SET_PLAYER_LIMIT = "playerLimit"

            const val SET_COMMAND = "setCommand"
            const val ADD_COMMAND = "addCommand"
            const val REMOVE_COMMAND = "removeCommand"
            const val LIST_COMMAND = "listCommand"

            const val SET_DURATION = "setDuration"
            const val ADD_DURATION = "addDuration"
            const val REMOVE_DURATION = "removeDuration"

            const val SET_PERMISSION = "permission"
            const val REQUIRED_PERMISSION = "requiredPermission"
            const val SET_PIN = "pin"

            const val SET_TARGET = "setTarget"
            const val ADD_TARGET = "addTarget"
            const val REMOVE_TARGET = "removeTarget"
            const val LIST_TARGET = "listTarget"

            const val SET_TEMPLATE = "setTemplate"
            const val SET_COOLDOWN = "cooldown"
            const val EDIT = "edit"
        }

        sealed interface Edit : JTab {
            companion object {
                const val REWARD = "reward"
                const val MESSAGE = "message"
                const val SOUND = "sound"
            }
        }

    }
    enum class Boolean(val value: String){
        True("true"), False("false")
    }

    enum class Delete(val value: String) {
        All("*"), Confirm("CONFIRM"), Last("LAST"),
    }
}
