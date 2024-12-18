package me.justlime.redeemX.enums

sealed interface JMessage {

    companion object {
        const val PREFIX = "prefix"
        const val RESTRICTED_TO_PLAYERS = "restricted-to-players"
        const val NO_PERMISSION = "no-permission"
    }

    sealed class Redeemed : JMessage {

        companion object {
            private const val REDEEMED = "redeemed-message"

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

    sealed class Commands : JMessage {

        data object Gen : Commands() {
            private const val GEN = "commands.gen"
            const val SUCCESS = "$GEN.success"
            const val INVALID_SYNTAX = "$GEN.invalid-syntax"
            const val INVALID_OPTIONS = "$GEN.invalid-options"
            const val INVALID_RANGE = "$GEN.invalid-range"
            const val NO_PERMISSION = "$GEN.no-permission"
            const val MISSING_PARAMETERS = "$GEN.missing-parameters"
            const val LENGTH_ERROR = "$GEN.length-error"
            const val CODE_ALREADY_EXIST = "$GEN.code-already-exist"
            const val FAILED = "$GEN.failed"
            const val ERROR = "$GEN.error"
        }

        data object GenTemplate: Commands(){
            private const val GEN_TEMPLATE = "commands.gen_template"
            const val SUCCESS = "$GEN_TEMPLATE.success"
            const val INVALID_SYNTAX = "$GEN_TEMPLATE.invalid-syntax"
            const val INVALID_OPTIONS = "$GEN_TEMPLATE.invalid-options"
            const val INVALID_RANGE = "$GEN_TEMPLATE.invalid-range"
            const val NO_PERMISSION = "$GEN_TEMPLATE.no-permission"
            const val MISSING_PARAMETERS = "$GEN_TEMPLATE.missing-parameters"
            const val LENGTH_ERROR = "$GEN_TEMPLATE.length-error"
            const val CODE_ALREADY_EXIST = "$GEN_TEMPLATE.code-already-exist"
            const val FAILED = "$GEN_TEMPLATE.failed"
            const val ERROR = "$GEN_TEMPLATE.error"
        }

        data object Modify : Commands() {
            private const val MODIFY = "commands.modify"
            const val SUCCESS = "$MODIFY.success"
            const val FAILED = "$MODIFY.failed"
            const val INVALID_SYNTAX = "$MODIFY.invalid-syntax"
            const val INVALID_VALUE = "$MODIFY.invalid-value"
            const val NOT_FOUND = "$MODIFY.not-found"
            const val NO_PERMISSION = "$MODIFY.no-permission"
            const val UNKNOWN_PROPERTY = "$MODIFY.unknown-property"
            const val INVALID_COMMAND = "$MODIFY.invalid-command"
            const val INVALID_ID = "$MODIFY.invalid-id"
            const val LIST = "$MODIFY.list"
            const val INVALID_SET = "$MODIFY.invalid-set"
            const val UNKNOWN_METHOD = "$MODIFY.unknown-method"
            const val ENABLED = "$MODIFY.enabled"
            const val EXPIRED_CODE = "$MODIFY.expired-code"
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
            const val TEMPLATE_UNSET = "$MODIFY.template_unset"

            const val CODE_GENERATE_DIGIT = "$MODIFY.code_generate_digit"
            const val DURATION = "$MODIFY.duration"
        }

        data object ModifyTemplate: Commands(){
            private const val MODIFY_TEMPLATE = "commands.modify_template"
            const val SUCCESS = "$MODIFY_TEMPLATE.success"
            const val FAILED = "$MODIFY_TEMPLATE.failed"
            const val INVALID_SYNTAX = "$MODIFY_TEMPLATE.invalid-syntax"
            const val INVALID_VALUE = "$MODIFY_TEMPLATE.invalid-value"
            const val NOT_FOUND = "$MODIFY_TEMPLATE.not-found"
            const val NO_PERMISSION = "$MODIFY_TEMPLATE.no-permission"
            const val UNKNOWN_PROPERTY = "$MODIFY_TEMPLATE.unknown-property"
            const val INVALID_COMMAND = "$MODIFY_TEMPLATE.invalid-command"
            const val INVALID_ID = "$MODIFY_TEMPLATE.invalid-id"
            const val LIST = "$MODIFY_TEMPLATE.list"
            const val INVALID_SET = "$MODIFY_TEMPLATE.invalid-set"
            const val UNKNOWN_METHOD = "$MODIFY_TEMPLATE.unknown-method"
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


        data object Delete : Commands() {
            private const val DELETE = "commands.delete"
            const val SUCCESS = "$DELETE.success"
            const val FAILED = "$DELETE.failed"
            const val NOT_FOUND = "$DELETE.not-found"
            const val NO_PERMISSION = "$DELETE.no-permission"
            const val INVALID_SYNTAX = "$DELETE.invalid-syntax"
        }

        data object DeleteTemplate : Commands() {
            private const val DELETE_TEMPLATE = "commands.delete_template"
            const val SUCCESS = "$DELETE_TEMPLATE.success"
            const val FAILED = "$DELETE_TEMPLATE.failed"
            const val NO_PERMISSION = "$DELETE_TEMPLATE.no-permission"
            const val INVALID_SYNTAX = "$DELETE_TEMPLATE.invalid-syntax"
        }
        data object DeleteAll : Commands() {
            private const val DELETE_ALL = "commands.delete_all"
            const val SUCCESS = "$DELETE_ALL.success"
            const val FAILED = "$DELETE_ALL.failed"
            const val NO_PERMISSION = "$DELETE_ALL.no-permission"
            const val INVALID_SYNTAX = "$DELETE_ALL.invalid-syntax"
            const val CONFIRMATION = "$DELETE_ALL.confirmation"
        }

        data object CommandList : Commands() {
            private const val LIST = "commands.list"
            const val HEADER = "$LIST.header"
            const val NO_CODES = "$LIST.no-codes"
            const val NO_PERMISSION = "$LIST.no-permission"
            const val FOOTER = "$LIST.footer"
        }

        data object ListTemplate : Commands() {
            private const val LIST_TEMPLATE = "commands.list_template"
            const val HEADER = "$LIST_TEMPLATE.header"
            const val NO_CODES = "$LIST_TEMPLATE.no-codes"
            const val NO_PERMISSION = "$LIST_TEMPLATE.no-permission"
            const val FOOTER = "$LIST_TEMPLATE.footer"
        }

        data object Renew : Commands() {
            private const val RENEW = "commands.renew"
            const val SUCCESS = "$RENEW.success"
            const val INVALID_SYNTAX = "$RENEW.invalid-syntax"
            const val NOT_FOUND = "$RENEW.not-found"
            const val NO_PERMISSION = "$RENEW.no-permission"
            const val FAILED = "$RENEW.failed"
            const val PLAYER_NOT_FOUND = "$RENEW.player-not-found"
        }

        data object Info : Commands() {
            private const val INFO = "commands.info"
            const val DETAILS = "$INFO.details"
            const val NOT_FOUND = "$INFO.not-found"
            const val NO_PERMISSION = "$INFO.no-permission"
        }

        data object Help : Commands() {
            private const val HELP = "commands.help"
            const val HEADER = "$HELP.header"
            const val COMMAND_DETAIL = "$HELP.command-detail"
            object CommandDetail {
                private const val DETAIL = "commands.help.command-detail"
                const val GEN = "$DETAIL.gen"
                const val MODIFY = "$DETAIL.modify"
                const val DELETE = "$DETAIL.delete"
                const val DELETE_ALL = "$DETAIL.delete_all"
                const val RENEW = "$DETAIL.renew"
                const val INFO = "$DETAIL.info"
            }
            const val FOOTER = "$HELP.footer"
            const val UNKNOWN_COMMAND = "$HELP.unknown-command"
        }
        data object Reload : Commands() {
            private const val RELOAD = "commands.reload"
            const val SUCCESS = "$RELOAD.success"
            const val FAILED = "$RELOAD.failed"
            const val NO_PERMISSION = "$RELOAD.no-permission"
        }

        companion object {
            const val UNKNOWN_COMMAND = "commands.unknown-command"
        }
    }
}


