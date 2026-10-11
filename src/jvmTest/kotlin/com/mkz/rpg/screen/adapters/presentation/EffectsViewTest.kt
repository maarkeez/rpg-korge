package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EffectsViewTest : ViewsForTesting() {
    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val effectsView = EffectsView(loadedResourcesLoader())
                // When
                effectsView.display(effectIds = listOf("venom-damage"))
                // Then
                assertThat(effectsView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should display one icon per effect when effects are displayed`() =
            viewsTest {
                // Given
                val effectsView = EffectsView(loadedResourcesLoader())
                // When
                effectsView.display(effectIds = listOf("venom-damage", "venom-on-death"))
                // Then
                assertThat(effectsView.displayedEffectCount()).isEqualTo(2)
            }

        @Test
        fun `should hide icons of previous effects when fewer effects are displayed`() =
            viewsTest {
                // Given
                val effectsView = EffectsView(loadedResourcesLoader())
                effectsView.display(effectIds = listOf("venom-damage", "venom-on-death", "venom-damage"))
                // When
                effectsView.display(effectIds = listOf("venom-damage"))
                // Then
                assertThat(effectsView.displayedEffectCount()).isEqualTo(1)
            }

        @Test
        fun `should display as many icons as effect slots when there are more effects than slots`() =
            viewsTest {
                // Given
                val effectsView = EffectsView(loadedResourcesLoader())
                val effectIds = List(effectsView.effectSlotCount() + 2) { "venom-damage" }
                // When
                effectsView.display(effectIds = effectIds)
                // Then
                assertThat(effectsView.displayedEffectCount()).isEqualTo(effectsView.effectSlotCount())
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val effectsView = EffectsView(loadedResourcesLoader())
                effectsView.display(effectIds = listOf("venom-damage"))
                // When
                effectsView.hide()
                // Then
                assertThat(effectsView.isVisibleToUser()).isFalse
            }
    }
}
