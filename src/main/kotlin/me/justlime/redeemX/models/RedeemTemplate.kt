package me.justlime.redeemX.models

data class RedeemTemplate(
    var name: String, //TODO Implement Template Rename
    var commands: MutableMap<Int,String>,
    var duration: String,
    var cooldown: String,
    var pin: Int = 0,
    var redemption: Int = 1,
    var playerLimit: Int = 1,
    var locked: Boolean,
    var permissionRequired: Boolean,
    var permissionValue: String = "",
    var message: List<String>
    )
