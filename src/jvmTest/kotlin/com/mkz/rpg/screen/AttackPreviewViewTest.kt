package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.unit.domain.UnitMother.unit
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.View
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AttackPreviewViewTest : ViewsForTesting() {
    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
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
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
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
                val unitPortraits = attackPreviewView.allViews().filterIsInstance<UnitPortraitView>()
                assertThat(unitPortraits.first().visible).isTrue
                assertThat(unitPortraits.last().visible).isFalse
            }

        @Test
        fun `should display receiver views when receiver battle unit is provided`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
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
                val unitPortraits = attackPreviewView.allViews().filterIsInstance<UnitPortraitView>()
                assertThat(unitPortraits.first().visible).isTrue
                assertThat(unitPortraits.last().visible).isTrue
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
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

    @Nested
    inner class ConfirmButtonClick {
        @Test
        fun `should notify delegate when confirm button is clicked`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
                val delegate = mock<AttackPreviewView.Delegate>()
                attackPreviewView.setDelegate(delegate)
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
                addChild(attackPreviewView)
                val confirmButton = attackPreviewView.allViews().filterIsInstance<UIButton>().single { it.text == "Confirm" }
                // When
                confirmButton.simulateClick()
                // Then
                verify(delegate).castConfirmed()
            }
    }

    @Nested
    inner class CancelButtonClick {
        @Test
        fun `should notify delegate when cancel button is clicked`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
                val delegate = mock<AttackPreviewView.Delegate>()
                attackPreviewView.setDelegate(delegate)
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
                addChild(attackPreviewView)
                val cancelButton = attackPreviewView.allViews().filterIsInstance<UIButton>().single { it.text == "Cancel" }
                // When
                cancelButton.simulateClick()
                // Then
                verify(delegate).castCancelled()
            }
    }
}

private fun Container.allViews(): List<View> {
    val views = mutableListOf<View>(this)
    children.forEach { child ->
        if (child is Container) views += child.allViews()
    }
    return views
}
