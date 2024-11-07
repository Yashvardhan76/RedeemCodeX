package me.justlime.redeemX.data.models

data class RedeemCode(
    val code: String,
    var commands: Map<Int, String> = mapOf(),
    var duration: String? = null,
    var isEnabled: Boolean = true,
    var max_redeems: Int = 1,
    var max_player: Int = 1,
    var max_redeems_per_player: Int = 1,
    var permission: String?,
    var pin: Int = -1,
    var target: String? = null,
    var usedBy: String? = null,
)