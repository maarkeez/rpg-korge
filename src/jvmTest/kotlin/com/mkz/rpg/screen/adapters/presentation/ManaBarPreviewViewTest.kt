package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.filledWidth
import com.mkz.rpg.shared.adapters.presentation.labelText
import com.mkz.rpg.shared.adapters.presentation.previewWidth
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ManaBarPreviewViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should be visible when is initialized`() =
            viewsTest {
                // Given
                val manaBarPreviewView = ManaBarPreviewView(size)
                // When
                // Then
                manaBarPreviewView.isVisibleToUser().shouldBeTrue()
            }
    }

    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val manaBarPreviewView = ManaBarPreviewView(size)
                manaBarPreviewView.hide()
                // When
                manaBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
                // Then
                manaBarPreviewView.isVisibleToUser().shouldBeTrue()
            }

        @Test
        fun `should modify label when is displayed`() =
            viewsTest {
                // Given
                val manaBarPreviewView = ManaBarPreviewView(size)
                // When
                manaBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
                // Then
                manaBarPreviewView.labelText() shouldBe "50 / 100"
            }

        @Test
        fun `should modify filled width when is displayed`() =
            viewsTest {
                // Given
                val manaBarPreviewView = ManaBarPreviewView(size)
                val remainingAfter = 50
                val maximum = 100
                val filledPercentage = (remainingAfter.toDouble() / maximum.toDouble())
                // When
                manaBarPreviewView.display(remainingBefore = 100, remainingAfter = remainingAfter, maximum = maximum)
                // Then
                manaBarPreviewView.filledWidth() shouldBe size.width * filledPercentage
            }

        @Test
        fun `should modify preview width when is displayed`() =
            viewsTest {
                // Given
                val manaBarPreviewView = ManaBarPreviewView(size)
                val remainingBefore = 75
                val maximum = 100
                val previewPercentage = (remainingBefore.toDouble() / maximum.toDouble())
                // When
                manaBarPreviewView.display(remainingBefore = remainingBefore, remainingAfter = 50, maximum = maximum)
                // Then
                manaBarPreviewView.previewWidth() shouldBe size.width * previewPercentage
            }

        @Test
        fun `should set filled width to zero when remaining after is negative`() =
            viewsTest {
                // Given
                val manaBarPreviewView = ManaBarPreviewView(size)
                // When
                manaBarPreviewView.display(remainingBefore = 100, remainingAfter = -10, maximum = 100)
                // Then
                manaBarPreviewView.filledWidth() shouldBe 0.00
                manaBarPreviewView.labelText() shouldBe "0 / 100"
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val manaBarPreviewView = ManaBarPreviewView(size)
                manaBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
                // When
                manaBarPreviewView.hide()
                // Then
                manaBarPreviewView.isVisibleToUser().shouldBeFalse()
            }
    }
}
