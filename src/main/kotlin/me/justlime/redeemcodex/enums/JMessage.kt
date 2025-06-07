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


package me.justlime.redeemcodex.enums

sealed interface JMessage {
    companion object {
        const val PREFIX = "prefix"
    }

    sealed interface Redeem : JMessage {
        companion object {
            const val INVALID_CODE = "redeem-invalid-code"
            const val MAX_REDEMPTIONS = "redeem-max-redemptions"
            const val MAX_PLAYER_REDEEMED = "redeem-max-player-redeemed"
            const val DISABLED = "redeem-disabled"
            const val EXPIRED_CODE = "redeem-expired-code"
            const val INVALID_TARGET = "redeem-invalid-target"
            const val MISSING_PIN = "redeem-missing-pin"
            const val INVALID_PIN = "redeem-invalid-pin"
            const val ALREADY_REDEEMED = "redeem-already-redeemed"
            const val COMMAND_COOLDOWN = "redeem-command-cooldown"
            const val ON_COOLDOWN = "redeem-on-cooldown"
            const val FULL_INVENTORY = "redeem-full-inventory"
            const val NO_PERMISSION = "redeem-no-permission"
            const val SUCCESS = "redeem-success"
            const val FAILED = "redeem-failed"
            const val USAGE = "redeem-usage"
        }

    }

    sealed interface Code : JMessage {
        companion object {
            const val NOT_FOUND = "code-not-found"
            const val DISABLED = "code-disabled"
        }

        data object Placeholder {
            const val DISABLED = "code-placeholder-disabled"
            const val ENABLED = "code-placeholder-enabled"
        }

        data object Generate : Code {
            const val SUCCESS = "code-gen-success"
            const val FAILED = "code-gen-failed"
            const val ALREADY_EXIST = "code-gen-already-exist"
            const val MISSING = "code-gene-missing"
            const val INVALID_AMOUNT = "code-gen-invalid-amount"
            const val INVALID_LENGTH = "code-gen-invalid-length"
            const val INVALID_RANGE = "code-gen-invalid-range"


        }

        data object Modify : Code {
            const val SUCCESS = "code-modify-success"
            const val FAILED = "code-modify-failed"
            const val INVALID_VALUE = "code-modify-invalid-value"
            const val INVALID_ID = "code-modify-invalid-id"
            const val NOT_FOUND = "code-modify-not-found"
            const val SYNC = "code-modify-sync"
            const val SYNC_LOCKED = "code-modify-sync-locked"
            const val ENABLED_STATUS = "code-modify-enabled-status"
            const val SYNC_STATUS = "code-modify-sync-status"
            const val SET_TEMPLATE = "code-modify-set-template"
            const val SET_REDEMPTION = "code-modify-set-redemption"
            const val SET_PLAYER_LIMIT = "code-modify-set-player-limit"
            const val SET_PIN = "code-modify-set-pin"
            const val SET_DURATION = "code-modify-set-duration"
            const val ADD_DURATION = "code-modify-add-duration"
            const val REMOVE_DURATION = "code-modify-remove-duration"
            const val SET_COOLDOWN = "code-modify-set-cooldown"
            const val SET_PERMISSION = "code-modify-set-permission"
            const val SET_TARGET = "code-modify-set-target"
            const val ADD_TARGET = "code-modify-add-target"
            const val REMOVE_TARGET = "code-modify-remove-target"
            const val SET_COMMAND = "code-modify-set-command"
            const val ADD_COMMAND = "code-modify-add-command"
            const val REMOVE_COMMAND = "code-modify-remove-command"
            const val REMOVE_ALL_COMMAND = "code-modify-remove-all-command"
        }

        data object Delete : Code {
            const val SUCCESS = "code-delete-success"
            const val SUCCESS_ALL = "code-delete-success-all"
            const val NOT_FOUND = "code-delete-not-found"
            const val NOT_FOUND_ALL = "code-delete-not-found-all"
            const val SUCCESS_CODES = "code-delete-success-codes"
            const val CONFIRMATION_NEEDED = "code-delete-confirmation-needed"
        }

        data object Renew : Code {
            const val SUCCESS = "code-renew-success"
            const val FAILED = "code-renew-failed"
            const val EXPIRED_CODE = "code-renew-expired-code"
            const val PLAYER_NOT_FOUND = "code-renew-player-not-found"
        }

        data object Preview : Code {
            const val PREVIEW = "code-preview"
        }

        data object Usages : Code {
            const val USAGE = "code-usages-usage"
            const val COMMAND = "code-usages-commands"
            const val TARGET = "code-usages-target-list"
        }

        data object Gui : JMessage {
            data object Save : JMessage {
                const val REWARDS = "code-gui-save-rewards"
                const val MESSAGE = "code-gui-save-message"
                const val SOUND = "code-gui-save-sound"
            }
        }
    }

    sealed interface Template : JMessage {

        companion object {
            const val NOT_FOUND = "template-not-found"
            const val DISABLED = "template-disabled"
            const val USAGE = "template-usage"
        }

        data object Placeholder {

        }

        data object Generate : Template {
            const val SUCCESS = "template-gen-success"
            const val FAILED = "template-gen-failed"
            const val ALREADY_EXIST = "template-gen-already-exist"
            const val MISSING = "template-gen-missing"
            const val INVALID_AMOUNT = "template-gen-invalid-amount"
            const val INVALID_LENGTH = "template-gen-invalid-length"
            const val INVALID_RANGE = "template-gen-invalid-range"
        }

        data object Modify : Template {
            const val SUCCESS = "template-modify-success"
            const val FAILED = "template-modify-failed"
            const val CODES_MODIFIED = "template-modify-codes-modified"
            const val INVALID_VALUE = "template-modify-invalid-value"
            const val INVALID_ID = "template-modify-invalid-id"
            const val NOT_FOUND = "template-modify-not-found"
            const val SET_PERMISSION = "template-modify-set-permission"
            const val ENABLED_PERMISSION = "template-modify-enabled-permission"
            const val DISABLED_PERMISSION = "template-modify-disabled-permission"
            const val SET_DURATION = "template-modify-set-duration"
            const val ADD_DURATION = "template-modify-add-duration"
            const val REMOVE_DURATION = "template-modify-remove-duration"

            const val SET_COOLDOWN = "template-modify-set-cooldown"

            const val SET_COMMAND = "template-modify-set-command"
            const val ADD_COMMAND = "template-modify-add-command"
            const val REMOVE_COMMAND = "template-modify-remove-command"
            const val REMOVE_ALL_COMMAND = "template-modify-remove-all-command"


            const val SYNC_LOCKED = "template-modify-sync-locked"
            const val SYNC_STATUS = "template-modify-locked-status"
            const val SET_DEFAULT_ENABLED_STATUS = "template-modify-default-enabled-status"
            const val SET_TEMPLATE = "template-modify-set-template"
            const val SET_REDEMPTION = "template-modify-set-redemption"
            const val SET_PLAYER_LIMIT = "template-modify-set-player-limit"
            const val SET_PIN = "template-modify-set-pin"
        }

        data object Delete : Template {
            const val SUCCESS = "template-delete-success"
            const val SUCCESS_ALL = "template-delete-success-all"
            const val NOT_FOUND = "template-delete-not-found"
            const val NOT_FOUND_ALL = "template-delete-not-found-all"
            const val CONFIRMATION_NEEDED = "template-delete-confirmation-needed"
            const val FAILED_DEFAULT = "template-delete-default-delete"
        }

        data object Usage: Template{

        }

        data object Preview : Code {
            const val PREVIEW = "template-preview"
        }

        data object Gui : JMessage {
            data object Save : JMessage {
                const val REWARDS = "gui-save-rewards"
                const val MESSAGE = "gui-save-message"
                const val SOUND = "gui-save-sound"
            }
        }
    }

    sealed interface Command : JMessage {
        companion object {
            const val RESTRICTED_TO_PLAYERS = "commands-restricted-to-players"
            const val NO_PERMISSION = "commands-no-permission"
            const val UNKNOWN_COMMAND = "commands-unknown-command"
            const val INFO = "commands-info"

        }

        data object Help : Command {
            const val GENERAL = "commands-help-general"
            const val REDEEM = "commands-help-redeem"
            const val GENERATION = "commands-help-generation"
            const val MODIFICATION = "commands-help-modification"
            const val DELETION = "commands-help-deletion"
            const val RENEWAL = "commands-help-renewal"
            const val PREVIEW = "commands-help-preview"
            const val USAGE = "commands-help-usage"
            const val RELOAD = "commands-help-reload"
            const val PERMISSIONS = "commands-help-permissions"
        }

        data object Renew : Command {
            const val SUCCESS = "commands-renew-success"
            const val INVALID_SYNTAX = "commands-renew-invalid-syntax"
            const val EXPIRED_CODE = "commands-renew-expired-code"
            const val FAILED = "commands-renew-failed"
            const val PLAYER_NOT_FOUND = "commands-renew-player-not-found"
        }

        data object Reload : Command {
            const val SUCCESS = "commands-reload-success"
            const val FAILED = "commands-reload-failed"
        }
    }

}
