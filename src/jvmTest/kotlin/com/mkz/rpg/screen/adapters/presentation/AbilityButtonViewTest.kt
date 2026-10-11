package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AbilityButtonViewTest : ViewsForTesting() {
    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
                // When
                abilityButtonView.display(abilityId = "mushroom", canCast = true)
                // Then
                assertThat(abilityButtonView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should display ability icon when is displayed`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
                // When
                abilityButtonView.display(abilityId = "mushroom", canCast = true)
                // Then
                assertThat(abilityButtonView.isAbilityIconDisplayed()).isTrue
            }

        @Test
        fun `should not darken ability icon when ability can be cast`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
                // When
                abilityButtonView.display(abilityId = "mushroom", canCast = true)
                // Then
                assertThat(abilityButtonView.isAbilityIconDarkened()).isFalse
            }

        @Test
        fun `should darken ability icon when ability can not be cast`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
                // When
                abilityButtonView.display(abilityId = "mushroom", canCast = false)
                // Then
                assertThat(abilityButtonView.isAbilityIconDarkened()).isTrue
            }
    }

    @Nested
    inner class Select {
        @Test
        fun `should display ability selection icon when is selected`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
                // When
                abilityButtonView.select()
                // Then
                assertThat(abilityButtonView.isSelected()).isTrue
            }

        @Test
        fun `should not display duplicated ability selection icon when is selected twice`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
                abilityButtonView.select()
                // When
                abilityButtonView.select()
                // Then
                assertThat(abilityButtonView.selectionIconCount()).isEqualTo(1)
            }
    }

    @Nested
    inner class Unselect {
        @Test
        fun `should remove ability selection icon when is unselected`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
                abilityButtonView.select()
                // When
                abilityButtonView.unselect()
                // Then
                assertThat(abilityButtonView.isSelected()).isFalse
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
                abilityButtonView.display(abilityId = "mushroom", canCast = true)
                // When
                abilityButtonView.hide()
                // Then
                assertThat(abilityButtonView.isVisibleToUser()).isFalse
            }
    }

    @Nested
    inner class ButtonClick {
        @Test
        fun `should notify delegate with ability id when button is clicked`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
                val delegate = mock<AbilityButtonView.Delegate>()
                abilityButtonView.setDelegate(delegate)
                abilityButtonView.display(abilityId = "mushroom", canCast = true)
                addChild(abilityButtonView)
                // When
                clickAbilityButton(abilityButtonView)
                // Then
                verify(delegate).abilitySelected(abilityId = "mushroom")
            }
    }
}
