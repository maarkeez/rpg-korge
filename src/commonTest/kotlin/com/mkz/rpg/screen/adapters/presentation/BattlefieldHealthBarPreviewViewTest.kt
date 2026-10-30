package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattlefieldHealthBarPreviewView.PreviewOption
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class BattlefieldHealthBarPreviewViewTest : ViewsForTesting() {
    private val size = Size(width = 48, height = 8)

    @Test
    fun `Display should blink one health segment when one out of three is previewed`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = BattlefieldHealthBarPreviewView(size)
            // When
            healthBarPreviewView.display(PreviewOption.ONE_OUT_OF_THREE)
            // Then
            healthBarPreviewView.blinkingCount() shouldBe 1
        }

    @Test
    fun `Display should stop blinking the previous preview when is displayed again`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = BattlefieldHealthBarPreviewView(size)
            healthBarPreviewView.display(PreviewOption.ONE_OUT_OF_THREE)
            val previousBlinks = healthBarPreviewView.blinks()
            // When
            healthBarPreviewView.display(PreviewOption.TWO_OUT_OF_THREE)
            // Then
            previousBlinks.none { blink -> blink.isBlinking() }.shouldBeTrue()
            healthBarPreviewView.blinkingCount() shouldBe 1
        }

    @Test
    fun `Display should not blink any health segment when the full health is previewed`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = BattlefieldHealthBarPreviewView(size)
            healthBarPreviewView.display(PreviewOption.EMPTY)
            // When
            healthBarPreviewView.display(PreviewOption.FULL)
            // Then
            healthBarPreviewView.blinkingCount() shouldBe 0
        }

    @Test
    fun `Hold should stop the blinks and draw the third left when the health is held`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = BattlefieldHealthBarPreviewView(size)
            healthBarPreviewView.display(PreviewOption.TWO_OUT_OF_THREE)
            val blinks = healthBarPreviewView.blinks()
            // When
            healthBarPreviewView.hold()
            // Then
            blinks.none { blink -> blink.isBlinking() }.shouldBeTrue()
            healthBarPreviewView.blinkedThirdAlphas() shouldBe listOf(1.0)
        }

    @Test
    fun `Hold should stop the blinks and not draw the third when the empty health is held`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = BattlefieldHealthBarPreviewView(size)
            healthBarPreviewView.display(PreviewOption.EMPTY)
            val blinks = healthBarPreviewView.blinks()
            // When
            healthBarPreviewView.hold()
            // Then
            blinks.none { blink -> blink.isBlinking() }.shouldBeTrue()
            healthBarPreviewView.blinkedThirdAlphas() shouldBe listOf(0.0)
        }

    @Test
    fun `Of should be empty when no health would be left`() {
        PreviewOption.of(remaining = 0, maximum = 30) shouldBe PreviewOption.EMPTY
    }

    @Test
    fun `Of should be one out of three when up to a third of the health would be left`() {
        PreviewOption.of(remaining = 1, maximum = 30) shouldBe PreviewOption.ONE_OUT_OF_THREE
        PreviewOption.of(remaining = 10, maximum = 30) shouldBe PreviewOption.ONE_OUT_OF_THREE
    }

    @Test
    fun `Of should be two out of three when up to two thirds of the health would be left`() {
        PreviewOption.of(remaining = 11, maximum = 30) shouldBe PreviewOption.TWO_OUT_OF_THREE
        PreviewOption.of(remaining = 20, maximum = 30) shouldBe PreviewOption.TWO_OUT_OF_THREE
    }

    @Test
    fun `Of should be three out of three when more than two thirds but not all the health would be left`() {
        PreviewOption.of(remaining = 21, maximum = 30) shouldBe PreviewOption.THREE_OUT_OF_THREE
        PreviewOption.of(remaining = 29, maximum = 30) shouldBe PreviewOption.THREE_OUT_OF_THREE
    }

    @Test
    fun `Of should be full when all the health would be left`() {
        PreviewOption.of(remaining = 30, maximum = 30) shouldBe PreviewOption.FULL
    }
}
