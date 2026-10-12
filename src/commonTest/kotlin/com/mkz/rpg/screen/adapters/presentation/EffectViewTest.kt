package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class EffectViewTest : ViewsForTesting() {
    private val size = Size(width = 16, height = 16)

    @Test
    fun `DisplayEffect should display effect icon when effect is displayed`() =
        sharedViewsTest {
            // Given
            val effectView = EffectView(size, loadedResourcesLoader())
            // When
            effectView.displayEffect("venom-damage")
            // Then
            effectView.isEffectIconDisplayed().shouldBeTrue()
        }

    @Test
    fun `DisplayEffect should replace previous effect icon when another effect is displayed`() =
        sharedViewsTest {
            // Given
            val effectView = EffectView(size, loadedResourcesLoader())
            effectView.displayEffect("venom-damage")
            // When
            effectView.displayEffect("venom-on-death")
            // Then
            effectView.effectIconCount() shouldBe 1
        }

    @Test
    fun `DisplayEffect should fail when effect has no image`() =
        sharedViewsTest {
            // Given
            val effectView = EffectView(size, loadedResourcesLoader())
            // When
            val effectDisplay = { effectView.displayEffect("unknown-effect") }
            // Then
            shouldThrow<IllegalStateException> { effectDisplay() }.message.shouldContain("effect/unknown-effect.png")
        }

    @Test
    fun `Hide should not display effect icon when is hidden`() =
        sharedViewsTest {
            // Given
            val effectView = EffectView(size, loadedResourcesLoader())
            effectView.displayEffect("venom-damage")
            // When
            effectView.hide()
            // Then
            effectView.isEffectIconDisplayed().shouldBeFalse()
        }
}
