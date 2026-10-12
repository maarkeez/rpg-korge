package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EffectViewTest : ViewsForTesting() {
    private val size = Size(width = 16, height = 16)

    @Nested
    inner class DisplayEffect {
        @Test
        fun `should display effect icon when effect is displayed`() =
            viewsTest {
                // Given
                val effectView = EffectView(size, loadedResourcesLoader())
                // When
                effectView.displayEffect("venom-damage")
                // Then
                effectView.isEffectIconDisplayed().shouldBeTrue()
            }

        @Test
        fun `should replace previous effect icon when another effect is displayed`() =
            viewsTest {
                // Given
                val effectView = EffectView(size, loadedResourcesLoader())
                effectView.displayEffect("venom-damage")
                // When
                effectView.displayEffect("venom-on-death")
                // Then
                effectView.effectIconCount() shouldBe 1
            }

        @Test
        fun `should fail when effect has no image`() =
            viewsTest {
                // Given
                val effectView = EffectView(size, loadedResourcesLoader())
                // When
                val effectDisplay = { effectView.displayEffect("unknown-effect") }
                // Then
                shouldThrow<IllegalStateException> { effectDisplay() }.message.shouldContain("effect/unknown-effect.png")
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not display effect icon when is hidden`() =
            viewsTest {
                // Given
                val effectView = EffectView(size, loadedResourcesLoader())
                effectView.displayEffect("venom-damage")
                // When
                effectView.hide()
                // Then
                effectView.isEffectIconDisplayed().shouldBeFalse()
            }
    }
}
