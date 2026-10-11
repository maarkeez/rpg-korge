package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.unit.domain.UnitMother.unit
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AttackPreviewViewTest : ViewsForTesting() {
    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
                val casterBattleUnit = battleUnit(unit = unit(id = "knight").toDto()).toDto()
                val casterUnit = unit(id = "knight").toDto()
                // When
                attackPreviewView.display(
                    casterBattleUnit = casterBattleUnit,
                    casterUnit = casterUnit,
                    manaCost = 10,
                    receiverBattleUnit = null,
                    receiverUnit = null,
                    damage = null,
                )
                // Then
                assertThat(attackPreviewView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should hide receiver views when no receiver battle unit is provided`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
                val casterBattleUnit = battleUnit(unit = unit(id = "knight").toDto()).toDto()
                val casterUnit = unit(id = "knight").toDto()
                // When
                attackPreviewView.display(
                    casterBattleUnit = casterBattleUnit,
                    casterUnit = casterUnit,
                    manaCost = 10,
                    receiverBattleUnit = null,
                    receiverUnit = null,
                    damage = null,
                )
                // Then
                assertThat(attackPreviewView.isCasterPortraitVisible()).isTrue
                assertThat(attackPreviewView.isReceiverPortraitVisible()).isFalse
            }

        @Test
        fun `should display receiver views when receiver battle unit is provided`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
                val casterBattleUnit = battleUnit(unit = unit(id = "knight").toDto()).toDto()
                val casterUnit = unit(id = "knight").toDto()
                val receiverBattleUnit = battleUnit(unit = unit(id = "rat").toDto()).toDto()
                val receiverUnit = unit(id = "rat").toDto()
                // When
                attackPreviewView.display(
                    casterBattleUnit = casterBattleUnit,
                    casterUnit = casterUnit,
                    manaCost = 10,
                    receiverBattleUnit = receiverBattleUnit,
                    receiverUnit = receiverUnit,
                    damage = 10,
                )
                // Then
                assertThat(attackPreviewView.isCasterPortraitVisible()).isTrue
                assertThat(attackPreviewView.isReceiverPortraitVisible()).isTrue
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
                val casterBattleUnit = battleUnit(unit = unit(id = "knight").toDto()).toDto()
                val casterUnit = unit(id = "knight").toDto()
                attackPreviewView.display(
                    casterBattleUnit = casterBattleUnit,
                    casterUnit = casterUnit,
                    manaCost = 10,
                    receiverBattleUnit = null,
                    receiverUnit = null,
                    damage = null,
                )
                // When
                attackPreviewView.hide()
                // Then
                assertThat(attackPreviewView.isVisibleToUser()).isFalse
            }
    }
}
