package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.filledWidth
import com.mkz.rpg.shared.adapters.presentation.labelText
import com.mkz.rpg.shared.adapters.presentation.previewWidth
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class HealthBarPreviewViewTest : ViewsForTesting() {
    @Test
    fun `Init should be visible when is initialized`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = HealthBarPreviewView(size)
            // When
            // Then
            healthBarPreviewView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = HealthBarPreviewView(size)
            healthBarPreviewView.hide()
            // When
            healthBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // Then
            healthBarPreviewView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should modify label when is displayed`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = HealthBarPreviewView(size)
            // When
            healthBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // Then
            healthBarPreviewView.labelText() shouldBe "50 / 100"
        }

    @Test
    fun `Display should modify filled width when is displayed`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = HealthBarPreviewView(size)
            val remainingAfter = 50
            val maximum = 100
            val filledPercentage = (remainingAfter.toDouble() / maximum.toDouble())
            // When
            healthBarPreviewView.display(remainingBefore = 100, remainingAfter = remainingAfter, maximum = maximum)
            // Then
            healthBarPreviewView.filledWidth() shouldBe size.width * filledPercentage
        }

    @Test
    fun `Display should modify preview width when is displayed`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = HealthBarPreviewView(size)
            val remainingBefore = 75
            val maximum = 100
            val previewPercentage = (remainingBefore.toDouble() / maximum.toDouble())
            // When
            healthBarPreviewView.display(remainingBefore = remainingBefore, remainingAfter = 50, maximum = maximum)
            // Then
            healthBarPreviewView.previewWidth() shouldBe size.width * previewPercentage
        }

    @Test
    fun `Display should set filled width to zero when remaining after is negative`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = HealthBarPreviewView(size)
            // When
            healthBarPreviewView.display(remainingBefore = 100, remainingAfter = -10, maximum = 100)
            // Then
            healthBarPreviewView.filledWidth() shouldBe 0.00
            healthBarPreviewView.labelText() shouldBe "0 / 100"
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val healthBarPreviewView = HealthBarPreviewView(size)
            healthBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // When
            healthBarPreviewView.hide()
            // Then
            healthBarPreviewView.isVisibleToUser().shouldBeFalse()
        }
}
