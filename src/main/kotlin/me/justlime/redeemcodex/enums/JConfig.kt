package me.justlime.redeemcodex.enums

sealed interface JConfig {

    companion object {
        const val LANG = "lang"
    }

    data object Code : JConfig {
        private const val CODE = "code"
        const val DISPLAY_AMOUNT = "$CODE.display-amount"
        const val MINIMUM_DIGIT = "$CODE.minimum-digit"
        const val MAXIMUM_DIGIT = "$CODE.maximum-digit"
    }

    data object Removal: JConfig{
        private const val REMOVAL = "auto-delete"
        const val EXPIRED_CODES = "$REMOVAL.expired-codes"
        const val REDEEMED_CODES = "$REMOVAL.redeemed-codes"
    }

    data object Redeem : JConfig{
        private const val REDEEM = "redeem-command"
        const val COOLDOWN = "$REDEEM.cooldown"
    }

    data object Rewards : JConfig{
        private const val REWARDS = "rewards"
        const val DROP = "$REWARDS.drop"
        const val SOUND = "$REWARDS.sound"

    }

    data object Renew : JConfig{
        private const val RENEW = "renew"
        const val RESET_EXPIRED = "$RENEW.reset-expired"
        const val RESET_DELAY = "$RENEW.reset-delay"
        const val CLEAR_USAGE = "$RENEW.clear-usage"
        const val CLEAR_REWARDS = "$RENEW.clear-rewards"
        const val CLEAR_COMMANDS = "$RENEW.clear-commands"
        const val REMOVE_PERMISSION_REQUIRED = "$RENEW.remove-permission-required"

    }

    data object Logger: JConfig{
        private const val LOGGER = "logger"
        const val GENERATE = "$LOGGER.generate"
        const val MODIFY = "$LOGGER.modify"
        const val DELETE = "$LOGGER.delete"
    }

    data object Database : JConfig{
        const val VERSION = "database.version"
    }

}