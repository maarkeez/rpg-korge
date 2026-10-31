package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.filledShare
import com.mkz.rpg.shared.adapters.presentation.labelText
import com.mkz.rpg.shared.adapters.presentation.previewShare
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ManaBarPreviewViewTest : ViewsForTesting() {
    @Test
    fun `Init should be visible when is initialized`() =
        sharedViewsTest {
            // Given
            val manaBarPreviewView = ManaBarPreviewView(size)
            // When
            // Then
            manaBarPreviewView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val manaBarPreviewView = ManaBarPreviewView(size)
            manaBarPreviewView.hide()
            // When
            manaBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // Then
            manaBarPreviewView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should display the value before and after the change when it is displayed`() =
        sharedViewsTest {
            // Given
            val manaBarPreviewView = ManaBarPreviewView(size)
            // When
            manaBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // Then
            manaBarPreviewView.labelText() shouldBe "100 → 50"
        }

    @Test
    fun `Display should fill the bar up to the value after the change when is displayed`() =
        sharedViewsTest {
            // Given
            val manaBarPreviewView = ManaBarPreviewView(size)
            val remainingAfter = 50
            val maximum = 100
            val filledPercentage = (remainingAfter.toDouble() / maximum.toDouble())
            // When
            manaBarPreviewView.display(remainingBefore = 100, remainingAfter = remainingAfter, maximum = maximum)
            // Then
            manaBarPreviewView.filledShare() shouldBe filledPercentage
        }

    @Test
    fun `Display should preview the bar up to the value before the change when it decreases`() =
        sharedViewsTest {
            // Given
            val manaBarPreviewView = ManaBarPreviewView(size)
            val remainingBefore = 75
            val maximum = 100
            val previewPercentage = (remainingBefore.toDouble() / maximum.toDouble())
            // When
            manaBarPreviewView.display(remainingBefore = remainingBefore, remainingAfter = 50, maximum = maximum)
            // Then
            manaBarPreviewView.previewShare() shouldBe previewPercentage
        }

    @Test
    fun `Display should fill nothing when remaining after is negative`() =
        sharedViewsTest {
            // Given
            val manaBarPreviewView = ManaBarPreviewView(size)
            // When
            manaBarPreviewView.display(remainingBefore = 100, remainingAfter = -10, maximum = 100)
            // Then
            manaBarPreviewView.filledShare() shouldBe 0.00
            manaBarPreviewView.labelText() shouldBe "100 → 0"
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val manaBarPreviewView = ManaBarPreviewView(size)
            manaBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // When
            manaBarPreviewView.hide()
            // Then
            manaBarPreviewView.isVisibleToUser().shouldBeFalse()
        }
}
