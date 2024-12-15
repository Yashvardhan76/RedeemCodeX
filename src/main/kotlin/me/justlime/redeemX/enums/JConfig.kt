package me.justlime.redeemX.enums

sealed interface JConfig {
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