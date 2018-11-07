package com.bakkenbaeck.sol.extension

fun Pair<Long, Long>.calculateProgress(value: Long): Double {
    val span = second - first
    val current = value - first
    return current.toDouble() / span.toDouble()
}