package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability.Status
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image
import korlibs.korge.view.filter.filter
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
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                // When
                abilityButtonView.display(availability("mushroom", Status.READY))
                // Then
                assertThat(abilityButtonView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should display ability icon when is displayed`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                // When
                abilityButtonView.display(availability("mushroom", Status.READY))
                // Then
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY)).isNotNull
            }

        @Test
        fun `should not darken ability icon when ability can be cast`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                // When
                abilityButtonView.display(availability("mushroom", Status.READY))
                // Then
                val abilityIcon = abilityButtonView.findViewByName(AbilityButtonView.ABILITY) as Image
                assertThat(abilityIcon.filter).isNull()
            }

        @Test
        fun `should darken ability icon when ability can not be cast`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                // When
                abilityButtonView.display(availability("mushroom", Status.COOLDOWN, cooldownTurnsLeft = 2))
                // Then
                val abilityIcon = abilityButtonView.findViewByName(AbilityButtonView.ABILITY) as Image
                assertThat(abilityIcon.filter).isNotNull
            }
    }

    @Nested
    inner class DisplayStatus {
        @Test
        fun `should display no status glyph when the ability is ready`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                // When
                abilityButtonView.display(availability("mushroom", Status.READY))
                // Then
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY_STATUS)).isNull()
            }

        @Test
        fun `should display the turns left number when the ability is on cooldown`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                // When
                abilityButtonView.display(availability("mushroom", Status.COOLDOWN, cooldownTurnsLeft = 2))
                // Then
                val number = abilityButtonView.findViewByName(AbilityButtonView.ABILITY_COOLDOWN_NUMBER) as PixelGlyphs.PixelNumberView
                assertThat(number.value).isEqualTo(2)
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY_MANA_GLYPH)).isNull()
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY_LOCK_GLYPH)).isNull()
            }

        @Test
        fun `should display the mana drop and the cost when the battle unit has not enough mana`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                // When
                abilityButtonView.display(availability("mushroom", Status.NOT_ENOUGH_MANA, cost = 10))
                // Then
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY_MANA_GLYPH)).isNotNull
                val cost = abilityButtonView.findViewByName(AbilityButtonView.ABILITY_COST_NUMBER) as PixelGlyphs.PixelNumberView
                assertThat(cost.value).isEqualTo(10)
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY_COOLDOWN_NUMBER)).isNull()
            }

        @Test
        fun `should display the lock glyph when the battle unit has no casts left`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                // When
                abilityButtonView.display(availability("mushroom", Status.NO_CASTS_LEFT))
                // Then
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY_LOCK_GLYPH)).isNotNull
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY_COOLDOWN_NUMBER)).isNull()
            }

        @Test
        fun `should replace the status glyph when the ability is displayed again as ready`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                abilityButtonView.display(availability("mushroom", Status.NO_CASTS_LEFT))
                // When
                abilityButtonView.display(availability("mushroom", Status.READY))
                // Then
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY_STATUS)).isNull()
            }
    }

    @Nested
    inner class Select {
        @Test
        fun `should display ability selection icon when is selected`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                // When
                abilityButtonView.select()
                // Then
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY_SELECTION)).isNotNull
            }

        @Test
        fun `should not display duplicated ability selection icon when is selected twice`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                abilityButtonView.select()
                // When
                abilityButtonView.select()
                // Then
                val abilitySelectionIcons =
                    abilityButtonView.children.count { child -> child.name == AbilityButtonView.ABILITY_SELECTION }
                assertThat(abilitySelectionIcons).isEqualTo(1)
            }
    }

    @Nested
    inner class Unselect {
        @Test
        fun `should remove ability selection icon when is unselected`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                abilityButtonView.select()
                // When
                abilityButtonView.unselect()
                // Then
                assertThat(abilityButtonView.findViewByName(AbilityButtonView.ABILITY_SELECTION)).isNull()
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                abilityButtonView.display(availability("mushroom", Status.READY))
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
                val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75))
                abilityButtonView.loadAssets()
                val delegate = mock<AbilityButtonView.Delegate>()
                abilityButtonView.setDelegate(delegate)
                abilityButtonView.display(availability("mushroom", Status.READY))
                addChild(abilityButtonView)
                // When
                abilityButtonView.simulateClick()
                // Then
                verify(delegate).abilitySelected(abilityId = "mushroom")
            }
    }

    private fun availability(
        abilityId: String,
        status: Status,
        cost: Int = 0,
        cooldownTurnsLeft: Int = 0,
    ) = AbilityAvailability(abilityId = abilityId, name = "Ability", cost = cost, cooldownTurnsLeft = cooldownTurnsLeft, status = status)
}
