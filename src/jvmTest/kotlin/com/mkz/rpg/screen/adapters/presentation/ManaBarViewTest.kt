package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.isLabelShown
import com.mkz.rpg.shared.adapters.presentation.isProgressBarShown
import com.mkz.rpg.shared.adapters.presentation.labelText
import com.mkz.rpg.shared.adapters.presentation.progressPercentage
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.junit.jupiter.api.Test

class ManaBarViewTest : ViewsForTesting() {
    @Test
    fun `Init should not be visible when is initialized`() =
        viewsTest {
            // Given
            val manaBarView = ManaBarView(Size(width = 100.00, height = 50.00))
            // When
            // Then
            manaBarView.isVisibleToUser().shouldBeFalse()
            manaBarView.isProgressBarShown().shouldBeFalse()
            manaBarView.isLabelShown().shouldBeFalse()
        }

    @Test
    fun `Display should be visible when is displayed`() =
        viewsTest {
            // Given
            val manaBarView = ManaBarView(Size(width = 100.00, height = 50.00))
            // When
            manaBarView.display(remaining = 50, maximum = 100)
            // Then
            manaBarView.isVisibleToUser().shouldBeTrue()
            manaBarView.isProgressBarShown().shouldBeTrue()
            manaBarView.isLabelShown().shouldBeTrue()
        }

    @Test
    fun `Display should modify label when is displayed`() =
        viewsTest {
            // Given
            val manaBarView = ManaBarView(Size(width = 100.00, height = 50.00))
            // When
            manaBarView.display(remaining = 50, maximum = 100)
            // Then
            manaBarView.labelText() shouldBe "50 / 100"
        }

    @Test
    fun `Display should modify progress bar when is displayed`() =
        viewsTest {
            // Given
            val manaBarView = ManaBarView(Size(width = 100.00, height = 50.00))
            val remaining = 50
            val maximum = 100
            val percentage = (remaining.toDouble() / maximum.toDouble()) * 100
            // When
            manaBarView.display(remaining = 50, maximum = 100)
            // Then
            manaBarView.progressPercentage() shouldBe percentage
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        viewsTest {
            // Given
            val manaBarView = ManaBarView(Size(width = 100.00, height = 50.00))
            manaBarView.display(remaining = 50, maximum = 100)
            // When
            manaBarView.hide()
            // Then
            manaBarView.isVisibleToUser().shouldBeFalse()
            manaBarView.isProgressBarShown().shouldBeFalse()
            manaBarView.isLabelShown().shouldBeFalse()
        }
}
