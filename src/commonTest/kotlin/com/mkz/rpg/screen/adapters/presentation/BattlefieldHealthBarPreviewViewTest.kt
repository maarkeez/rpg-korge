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
}
