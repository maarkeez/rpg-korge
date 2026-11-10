package com.mkz.rpg.shared.domain

import kotlin.random.Random

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

    companion object {
        fun cpuBattleUnitsAlwaysResting(): FixedDoublesRandom = FixedDoublesRandom(0.0)
    }
}
