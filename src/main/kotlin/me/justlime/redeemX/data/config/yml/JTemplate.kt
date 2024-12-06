package me.justlime.redeemX.data.config.yml

interface JTemplate {
    data object Default : JTemplate{
        const val NAME = "default"
        const val COMMANDS = "$NAME.commands"

    }
    companion object {
        const val COMMANDS = "commands"
        const val DURATION = "duration"
        const val ENABLED = "enabled"
        const val MAX_REDEEMS = "max_redeems"
        const val MAX_PLAYERS = "max_players"
        const val PERMISSION_REQUIRED = "permissionRequired"
        const val PERMISSION_VALUE = "permissionValue"
        const val PIN = "pin"
        const val CODE_GENERATE_DIGIT = "codeGenerateDigit"
        const val CODE_EXPIRED_DURATION = "codeExpiredDuration"
    }

}