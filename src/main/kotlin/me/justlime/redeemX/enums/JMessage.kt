package me.justlime.redeemX.enums

sealed interface JMessage {
    companion object {
        const val PREFIX = "prefix"
        const val RESTRICTED_TO_PLAYERS = "restricted-to-players"
        const val NO_PERMISSION = "no-permission"
    }

    sealed class Redeem : JMessage {
        companion object {
            private const val REDEEMED = "redeemed-message"
            const val FULL_INVENTORY = "$REDEEMED.full-inventory"

            const val SUCCESS = "$REDEEMED.success"
            const val FAILED = "$REDEEMED.failed"
            const val USAGE = "$REDEEMED.usage"
            const val INVALID_CODE = "$REDEEMED.invalid-code"
            const val ALREADY_REDEEMED = "$REDEEMED.already-redeemed"
            const val EXPIRED_CODE = "$REDEEMED.expired-code"
            const val INVALID_PIN = "$REDEEMED.invalid-pin"
            const val NO_PERMISSION = "$REDEEMED.no-permission"
            const val DISABLED = "$REDEEMED.disabled"
            const val MISSING_PIN = "$REDEEMED.missing-pin"
            const val INVALID_TARGET = "$REDEEMED.invalid-target"
            const val MAX_PLAYER_REDEEMED = "$REDEEMED.max-player-redeemed"
            const val MAX_REDEMPTIONS = "$REDEEMED.max-redemptions"
            const val ON_COOLDOWN = "$REDEEMED.on-cooldown"
        }
    }

    sealed class RCX : JMessage {
        data object Gen : RCX() {
            private const val GEN = "commands.gen"
            const val MISSING = "$GEN.missing"
            const val INVALID_AMOUNT = "$GEN.invalid-amount"
            data object Code : RCX(){
                const val SUCCESS = "$GEN.code.success"
                const val FAILED = "$GEN.code.failed"
                const val ALREADY_EXIST = "$GEN.code.already-exist"
                const val INVALID_CODE = "$GEN.code.invalid-code"
                const val INVALID_TEMPLATE = "$GEN.code.invalid-template"
                const val INVALID_LENGTH = "$GEN.code.invalid-length"
                const val INVALID_RANGE = "$GEN.code.invalid-range"
            }

            data object Template : RCX(){
                const val SUCCESS = "$GEN.template.success"
                const val FAILED = "$GEN.template.failed"
                const val ALREADY_EXIST = "$GEN.template.already-exist"
                const val TEMPLATE_NOT_FOUND = "$GEN.template.not-found "
            }
        }

        data object GenTemplate : RCX() {
            private const val GEN_TEMPLATE = "commands.gen_template"
            const val SUCCESS = "$GEN_TEMPLATE.success"
            const val FAILED = "$GEN_TEMPLATE.failed"
            const val LENGTH_ERROR = "$GEN_TEMPLATE.length-error"
            const val ERROR = "$GEN_TEMPLATE.error"
            const val ALREADY_EXIST = "$GEN_TEMPLATE.already_exist"
        }

        data object Modify : RCX() {
            private const val MODIFY = "commands.modify"
            const val SUCCESS = "$MODIFY.success"
            const val FAILED = "$MODIFY.failed"
            const val INVALID_VALUE = "$MODIFY.invalid-value"
            const val NOT_FOUND = "$MODIFY.not-found"
            const val UNKNOWN_PROPERTY = "$MODIFY.unknown-property"
            const val INVALID_COMMAND = "$MODIFY.invalid-command"
            const val INVALID_ID = "$MODIFY.invalid-id"
            const val LIST = "$MODIFY.list"
            const val INVALID_SET = "$MODIFY.invalid-set"
            const val UNKNOWN_METHOD = "$MODIFY.unknown-method"
            const val ENABLED = "$MODIFY.enabled"
            const val EXPIRED_CODE = "$MODIFY.expired-code"
            const val LOCKED = "$MODIFY.locked"

            object Target {
                private const val TARGET = "commands.modify.target"
                const val ADD = "$TARGET.add"
                const val SET = "$TARGET.set"
                const val REMOVE = "$TARGET.remove"
                const val REMOVE_ALL = "$TARGET.remove-all"
                const val LIST = "$TARGET.list"
                const val UNKNOWN_METHOD = "$TARGET.unknown-method"
            }

            const val PIN = "$MODIFY.set_pin"
            const val MAX_REDEEMS = "$MODIFY.max_redeems"
            const val MAX_PLAYERS = "$MODIFY.max_players"
            const val SET_PERMISSION = "$MODIFY.permission"
            const val UNSET_PERMISSION = "$MODIFY.permission-disabled"
            const val TARGET = "$MODIFY.target"
            const val COOLDOWN = "$MODIFY.cooldown"
            const val TEMPLATE_SET = "$MODIFY.template-set"
            const val TEMPLATE_EMPTY = "$MODIFY.template-empty"
            const val TEMPLATE_INVALID = "$MODIFY.template-invalid"
            const val TEMPLATE_LOCKED = "$MODIFY.template_locked"

            const val CODE_GENERATE_DIGIT = "$MODIFY.code_generate_digit"
            const val DURATION = "$MODIFY.duration"

            object Edit {
                private const val EDIT = "commands.modify.edit"
                const val REWARDS = "$EDIT.rewards"
                const val MESSAGE = "$EDIT.message"
                const val SOUND = "$EDIT.sound"
            }
        }

        data object ModifyTemplate : RCX() {
            private const val MODIFY_TEMPLATE = "commands.modify_template"
            const val SUCCESS = "$MODIFY_TEMPLATE.success"
            const val FAILED = "$MODIFY_TEMPLATE.failed"
            const val INVALID_VALUE = "$MODIFY_TEMPLATE.invalid-value"
            const val NOT_FOUND = "$MODIFY_TEMPLATE.not-found"
            const val CODES_MODIFIED = "$MODIFY_TEMPLATE.CODES_MODIFIED"

            object Target {
                private const val TARGET = "commands.modify_template.target"
                const val ADD = "$TARGET.add"
                const val SET = "$TARGET.set"
                const val REMOVE = "$TARGET.remove"
                const val REMOVE_ALL = "$TARGET.remove-all"
                const val LIST = "$TARGET.list"
                const val UNKNOWN_METHOD = "$TARGET.unknown-method"
            }

            const val PIN = "$MODIFY_TEMPLATE.set_pin"
            const val MAX_REDEEMS = "$MODIFY_TEMPLATE.max_redeems"
            const val MAX_PLAYERS = "$MODIFY_TEMPLATE.max_players"
            const val PERMISSION = "$MODIFY_TEMPLATE.permission"
            const val ENABLED = "$MODIFY_TEMPLATE.enabled"
            const val CODE_GENERATE_DIGIT = "$MODIFY_TEMPLATE.code_generate_digit"
            const val COOLDOWN = "$MODIFY_TEMPLATE.cooldown"
            const val DURATION = "$MODIFY_TEMPLATE.duration"
        }

        data object Delete : RCX() {
            private const val DELETE = "commands.delete"

            object Success {
                const val CODES = "$DELETE.success.codes"
                const val ALL = "$DELETE.success.all"
            }

            object NotFound {
                const val CODES = "$DELETE.not-found.codes"
                const val ALL = "$DELETE.not-found.all"
            }

            const val CONFIRMATION_NEEDED = "$DELETE.confirmation-needed"
            const val FAILED = "$DELETE.failed"
        }

        data object DeleteTemplate : RCX() {
            private const val DELETE_TEMPLATE = "commands.delete_template"
            const val SUCCESS = "$DELETE_TEMPLATE.success"
            const val SUCCESS_ALL = "$DELETE_TEMPLATE.success_all"
            const val FAILED = "$DELETE_TEMPLATE.failed"
            const val NOT_FOUND = "$DELETE_TEMPLATE.not-found"
        }

        data object Renew : RCX() {
            private const val RENEW = "commands.renew"
            const val SUCCESS = "$RENEW.success"
            const val NOT_FOUND = "$RENEW.not-found"
            const val FAILED = "$RENEW.failed"
            const val PLAYER_NOT_FOUND = "$RENEW.player-not-found"
        }


        data object Help : RCX() {
            private const val HELP = "commands.help"
            const val UNKNOWN_COMMAND = "$HELP.unknown-command"
            const val GENERAL = "$HELP.general"
            const val REDEEM = "$HELP.redeem"
            const val GENERATION = "$HELP.generation"
            const val MODIFICATION = "$HELP.modification"
            const val DELETION = "$HELP.deletion"
            const val RENEWAL = "$HELP.renewal"
            const val PREVIEW = "$HELP.preview"
            const val USAGE = "$HELP.usage"
            const val PERMISSIONS = "$HELP.permissions"
            const val RELOAD = "$HELP.reload"
            const val INFO = "commands.info"
        }

        data object Usage: RCX(){
            private const val USAGE = "commands.usage"
            const val CODE = "$USAGE.code"
            const val TEMPLATE = "$USAGE.template"
            const val CODE_NOT_FOUND ="$USAGE.code-not-found"
            const val TEMPLATE_NOT_FOUND ="$USAGE.template-not-found"
        }

        data object Reload : RCX() {
            private const val RELOAD = "commands.reload"
            const val SUCCESS = "$RELOAD.success"
            const val FAILED = "$RELOAD.failed"
        }

        companion object {
            const val UNKNOWN_COMMAND = "commands.unknown-command"
        }
    }
}
