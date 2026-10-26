package com.mkz.rpg.shared.adapters.presentation

import io.kotest.matchers.shouldBe
import korlibs.math.geom.Size
import kotlin.test.Test

class WholePixelScaleTest {
    private val stage = Size(390, 844)

    @Test
    fun `should not scale the stage when the window is its size`() {
        WholePixelScale(stage, Size(390, 844)) shouldBe Size(390, 844)
    }

    @Test
    fun `should scale the stage by the screen's pixel ratio when the window is its size on a 2x or 3x screen`() {
        WholePixelScale(stage, Size(780, 1688)) shouldBe Size(780, 1688)
        WholePixelScale(stage, Size(1170, 2532)) shouldBe Size(1170, 2532)
    }

    @Test
    fun `should scale the stage by the largest whole number fitting the window`() {
        WholePixelScale(stage, Size(1000, 2000)) shouldBe Size(780, 1688)
    }

    @Test
    fun `should scale the stage by the largest whole number fitting both sides of the window`() {
        WholePixelScale(stage, Size(2000, 1700)) shouldBe Size(780, 1688)
    }

    @Test
    fun `should scale the stage down to fit when the window is smaller than it`() {
        WholePixelScale(stage, Size(195, 1000)) shouldBe Size(195, 422)
    }
}
