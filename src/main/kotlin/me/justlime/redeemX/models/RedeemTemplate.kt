package me.justlime.redeemX.models

data class RedeemTemplate(
    val name: String, //TODO Implement Template Rename
    var commands: MutableMap<Int,String>,
    var duration: String,
    var cooldown: String,
    var pin: Int = 0,
    var maxRedeems: Int = 1,
    var maxPlayers: Int = 1,
    var templateLocked: Boolean,
    var permissionRequired: Boolean,
    var permissionValue: String = "",
    var codeGenerateDigit: Int = 5,
    var message: List<String>
    )
