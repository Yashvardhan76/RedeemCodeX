package me.justlime.redeemX.data.config.yml

sealed interface JConfig {
    data object Default : JConfig {
        const val CODE_GENERATE_DIGIT = "code-generate-digit"
        const val CODE_EXPIRED_DURATION = "code-expired-duration"
        const val ENABLED = "enabled"
        const val MAX_REDEEMS = "max_redeems"
        const val MAX_PLAYERS_CAN_REDEEM = "max_players_can_redeem"
        data object PERMISSION : JConfig{
            const val REQUIRED = "required"
            const val VALUE = "value"
        }
        const val PIN = "pin"
        const val COMMANDS = "commands"
    }

    data object Modify : JConfig {
        const val PERMISSION = "permission"
    }

    data object Code : JConfig {
        const val MINIMUM_DIGIT = "code-minimum-digit"
        const val MAXIMUM_DIGIT = "code-maximum-digit"
    }

    data object AutoDelete : JConfig {
        const val EXPIRED_CODES = "auto-delete-expired-codes"
        const val REDEEMED_CODES = "auto-delete-redeemed-code-"
        const val REMOVE_PERMISSION = "auto-remove-permission-when-redeemed"
    }

    data object Cooldown : JConfig {
        const val COOLDOWN = "cooldown"
    }

    data object Renew : JConfig {
        const val RESET_EXPIRED = "reset-expired"
        const val RESET_DELAY = "reset-delay"
        const val CLEAR_USAGE = "clear-usage"
        const val CLEAR_REWARDS = "clear-rewards"
        const val CLEAR_COMMANDS = "clear-commands"
        const val REMOVE_PERMISSION_REQUIRED = "remove-permission-required"
    }

    data object Bot : JConfig {
        const val ENABLED = "enabled"
        const val TOKEN = "token"
    }

    data object Database : JConfig {
        const val VERSION = "database-version"
    }

}