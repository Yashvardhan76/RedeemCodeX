package me.justlime.redeemX.models

data class RedeemTemplate(
    val name: String,
    val commands: String = "",
    val duration: String = "",
    var isEnabled: Boolean = true,
    var maxRedeems: Int = 1,
    var maxPlayers: Int = 1,
    var permissionRequired: Boolean = false,
    var permissionValue: String = "",
    var pin: Int = 0,
    var codeGenerateDigit: Int = 5,
    val codeExpiredDuration: String = "",
    val templateLocked: Boolean = false,
    val cooldown: String = ""
    )
