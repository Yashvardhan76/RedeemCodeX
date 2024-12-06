package me.justlime.redeemX.data.models

data class RedeemTemplate(
    val name: String,
    val commands: String,
    val duration: String,
    val isEnabled: Boolean,
    val maxRedeems: Int,
    val maxPlayers: Int,
    val permissionRequired: Boolean,
    val permissionValue: String,
    val pin: Int,
    val codeGenerateDigit: Int,
    val codeExpiredDuration: String,
    )
