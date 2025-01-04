package me.justlime.redeemcodex.utilities

fun List<String>.toIndexedMap(): MutableMap<Int, String> {
    return mapIndexed { index, value -> index to value }.toMap().toMutableMap()
}
