package me.justlime.redeemX.data.models

data class RedeemTemplate(
    val name: String,
    val commands: String = "",
    val duration: String = "",
    val isEnabled: Boolean = true,
    val maxRedeems: Int = 1,
    val maxPlayers: Int = 1,
    val permissionRequired: Boolean = false,
    val permissionValue: String = "",
    val pin: Int = 0,
    val codeGenerateDigit: Int = 5,
    val codeExpiredDuration: String = "",
    val templateLocked: Boolean = false,
    val cooldown: String = ""
    )
