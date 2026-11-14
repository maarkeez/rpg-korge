package com.mkz.tessavar.shared.domain

import kotlin.random.Random

class FixedRandom(
    private val roll: Int,
) : Random() {
    override fun nextInt(until: Int): Int {
        require(roll in 0 until until) { "The fixed roll $roll must be between 0 and ${until - 1}" }
        return roll
    }

    override fun nextBits(bitCount: Int): Int = throw UnsupportedOperationException("Only nextInt(until) is fixed")
}
