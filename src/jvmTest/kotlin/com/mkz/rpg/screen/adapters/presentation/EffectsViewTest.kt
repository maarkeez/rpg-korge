package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class EffectsViewTest : ViewsForTesting() {
    @Test
    fun `Display should be visible when is displayed`() =
        viewsTest {
            // Given
            val effectsView = EffectsView(loadedResourcesLoader())
            // When
            effectsView.display(effectIds = listOf("venom-damage"))
            // Then
            effectsView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should display one icon per effect when effects are displayed`() =
        viewsTest {
            // Given
            val effectsView = EffectsView(loadedResourcesLoader())
            // When
            effectsView.display(effectIds = listOf("venom-damage", "venom-on-death"))
            // Then
            effectsView.displayedEffectCount() shouldBe 2
        }

    @Test
    fun `Display should hide icons of previous effects when fewer effects are displayed`() =
        viewsTest {
            // Given
            val effectsView = EffectsView(loadedResourcesLoader())
            effectsView.display(effectIds = listOf("venom-damage", "venom-on-death", "venom-damage"))
            // When
            effectsView.display(effectIds = listOf("venom-damage"))
            // Then
            effectsView.displayedEffectCount() shouldBe 1
        }

    @Test
    fun `Display should display as many icons as effect slots when there are more effects than slots`() =
        viewsTest {
            // Given
            val effectsView = EffectsView(loadedResourcesLoader())
            val effectIds = List(effectsView.effectSlotCount() + 2) { "venom-damage" }
            // When
            effectsView.display(effectIds = effectIds)
            // Then
            effectsView.displayedEffectCount() shouldBe effectsView.effectSlotCount()
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        viewsTest {
            // Given
            val effectsView = EffectsView(loadedResourcesLoader())
            effectsView.display(effectIds = listOf("venom-damage"))
            // When
            effectsView.hide()
            // Then
            effectsView.isVisibleToUser().shouldBeFalse()
        }
}
