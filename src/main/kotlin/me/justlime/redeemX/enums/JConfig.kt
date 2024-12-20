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
        private const val RENEW = "renew"
        const val RESET_EXPIRED = "$RENEW.reset-expired"
        const val RESET_DELAY = "$RENEW.reset-delay"
        const val CLEAR_USAGE = "$RENEW.clear-usage"
        const val CLEAR_REWARDS = "$RENEW.clear-rewards"
        const val CLEAR_COMMANDS = "$RENEW.clear-commands"
        const val REMOVE_PERMISSION_REQUIRED = "remove-permission-required"
    }

    data object Bot : JConfig {
        const val ENABLED = "bot.enabled"
        const val TOKEN = "bot.token"
    }

    data object Database : JConfig {
        const val VERSION = "database-version"
    }

}