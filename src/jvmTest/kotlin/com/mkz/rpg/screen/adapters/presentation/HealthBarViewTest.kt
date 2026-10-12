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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HealthBarViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should not be visible when is initialized`() =
            viewsTest {
                // Given
                val healthBarView = HealthBarView(Size(width = 100.00, height = 50.00))
                // When
                // Then
                healthBarView.isVisibleToUser().shouldBeFalse()
                healthBarView.isProgressBarShown().shouldBeFalse()
                healthBarView.isLabelShown().shouldBeFalse()
            }
    }

    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val healthBarView = HealthBarView(Size(width = 100.00, height = 50.00))
                // When
                healthBarView.display(remaining = 50, maximum = 100)
                // Then
                healthBarView.isVisibleToUser().shouldBeTrue()
                healthBarView.isProgressBarShown().shouldBeTrue()
                healthBarView.isLabelShown().shouldBeTrue()
            }

        @Test
        fun `should modify label when is displayed`() =
            viewsTest {
                // Given
                val healthBarView = HealthBarView(Size(width = 100.00, height = 50.00))
                // When
                healthBarView.display(remaining = 50, maximum = 100)
                // Then
                healthBarView.labelText() shouldBe "50 / 100"
            }

        @Test
        fun `should modify progress bar when is displayed`() =
            viewsTest {
                // Given
                val healthBarView = HealthBarView(Size(width = 100.00, height = 50.00))
                val remaining = 50
                val maximum = 100
                val percentage = (remaining.toDouble() / maximum.toDouble()) * 100
                // When
                healthBarView.display(remaining = 50, maximum = 100)
                // Then
                healthBarView.progressPercentage() shouldBe percentage
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val healthBarView = HealthBarView(Size(width = 100.00, height = 50.00))
                healthBarView.display(remaining = 50, maximum = 100)
                // When
                healthBarView.hide()
                // Then
                healthBarView.isVisibleToUser().shouldBeFalse()
                healthBarView.isProgressBarShown().shouldBeFalse()
                healthBarView.isLabelShown().shouldBeFalse()
            }
    }
}
