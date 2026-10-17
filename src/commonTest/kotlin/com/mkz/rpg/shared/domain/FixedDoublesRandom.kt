package com.mkz.rpg.shared.domain

import kotlin.random.Random

// Rolls the given doubles in order, repeating the last one, instead of random ones, so tests can choose the outcome of
// each roll between 0 and 1. The other rolls (e.g.: a random element of a list) stay random.
class FixedDoublesRandom(
    vararg doubles: Double,
) : Random() {
    private val doubles = doubles.toList()
    private var rolls = 0

    init {
        require(this.doubles.isNotEmpty()) { "At least one double must be rolled" }
    }

    override fun nextDouble(): Double = doubles[minOf(rolls++, doubles.lastIndex)]

    override fun nextBits(bitCount: Int): Int = Default.nextBits(bitCount)
}
