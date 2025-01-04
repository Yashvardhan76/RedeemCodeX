package me.justlime.redeemcodex.enums

import me.justlime.redeemcodex.models.RedeemCode
import me.justlime.redeemcodex.models.RedeemTemplate

sealed class RedeemType {
    data class Code(val redeemCode: RedeemCode) : RedeemType()
    data class Template(val redeemTemplate: RedeemTemplate) : RedeemType()
}