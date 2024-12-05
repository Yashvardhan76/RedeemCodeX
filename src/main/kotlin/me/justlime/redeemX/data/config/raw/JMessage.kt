package me.justlime.redeemX.data.config.raw

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
        }


        data object Delete : Commands() {
            private const val DELETE = "commands.delete"
            const val SUCCESS = "$DELETE.success"
            const val FAILED = "$DELETE.failed"
            const val NOT_FOUND = "$DELETE.not-found"
            const val NO_PERMISSION = "$DELETE.no-permission"
            const val INVALID_SYNTAX = "$DELETE.invalid-syntax"
        }

        data object DeleteAll : Commands() {
            private const val DELETE_ALL = "commands.delete_all"
            const val SUCCESS = "$DELETE_ALL.success"
            const val FAILED = "$DELETE_ALL.failed"
            const val NO_PERMISSION = "$DELETE_ALL.no-permission"
            const val INVALID_SYNTAX = "$DELETE_ALL.invalid-syntax"
            const val CONFIRMATION = "$DELETE_ALL.confirmation"
        }


        data object Renew : Commands() {
            private const val RENEW = "commands.renew"
            const val SUCCESS = "$RENEW.success"
            const val INVALID_SYNTAX = "$RENEW.invalid-syntax"
            const val NOT_FOUND = "$RENEW.not-found"
            const val EXPIRED_CODE = "$RENEW.expired-code"
            const val NO_PERMISSION = "$RENEW.no-permission"

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


//fun sample(){
//    Messages.Commands.Gen.SUCCESS
//    Messages.Commands.Modify.SUCCESS
//
//}
//I am not mad i just used chat gpt
//But then also took 30m oof


