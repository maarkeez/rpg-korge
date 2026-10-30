package com.mkz.rpg.shared.adapters.presentation

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.image.color.RGBA
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class GameButtonTest : ViewsForTesting() {
    @Test
    fun `Init should not be faded when is initialized`() =
        sharedViewsTest {
            // Given
            // When
            val button = gameButton("Close", Size(80, 32))
            // Then
            button.isFaded().shouldBeFalse()
        }

    @Test
    fun `Enabled should fade the button when is disabled`() =
        sharedViewsTest {
            // Given
            val button = gameButton("Close", Size(80, 32))
            // When
            button.enabled = false
            // Then
            button.isFaded().shouldBeTrue()
            button.backgroundColorWhenDisabled() shouldBe GameButton.DISABLED
        }

    @Test
    fun `Enabled should not be faded when is enabled again`() =
        sharedViewsTest {
            // Given
            val button = gameButton("Close", Size(80, 32))
            button.enabled = false
            // When
            button.enabled = true
            // Then
            button.isFaded().shouldBeFalse()
        }

    @Test
    fun `Enabled should fade a button of another color as the grey ones when is disabled`() =
        sharedViewsTest {
            // Given
            val button = gameButton("Finish turn", Size(390, 32), color = RGBA(0, 136, 255))
            // When
            button.enabled = false
            // Then
            button.isFaded().shouldBeTrue()
            button.backgroundColorWhenDisabled() shouldBe GameButton.DISABLED
        }
}
