package com.mkz.rpg.screen.adapters.presentation

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class GoldTextTest {
    @Test
    fun `GoldText should write the price in gold`() {
        goldText(50) shouldBe "50 G"
    }

    @Test
    fun `GoldText should write the price in gold when the item is worthless`() {
        goldText(0) shouldBe "0 G"
    }
}
