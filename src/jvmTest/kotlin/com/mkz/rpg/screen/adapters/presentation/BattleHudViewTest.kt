package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BattleHudViewTest : ViewsForTesting() {
    @Nested
    inner class DisplayBattleUnitInfoView {
        @Test
        fun `should display battle unit info view when is displayed`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
                val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
                val battleHudView = BattleHudView(Size(width = 400.00, height = 200.00), battleUnitInfoView, attackPreviewView)
                battleHudView.displayAttackPreviewView()
                // When
                battleHudView.displayBattleUnitInfoView()
                // Then
                assertThat(battleHudView.children).containsExactly(battleUnitInfoView)
            }
    }

    @Nested
    inner class DisplayAttackPreviewView {
        @Test
        fun `should display attack preview view when is displayed`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
                val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
                val battleHudView = BattleHudView(Size(width = 400.00, height = 200.00), battleUnitInfoView, attackPreviewView)
                battleHudView.displayBattleUnitInfoView()
                // When
                battleHudView.displayAttackPreviewView()
                // Then
                assertThat(battleHudView.children).containsExactly(attackPreviewView)
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not display any view when is hidden`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
                val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
                val battleHudView = BattleHudView(Size(width = 400.00, height = 200.00), battleUnitInfoView, attackPreviewView)
                battleHudView.displayBattleUnitInfoView()
                // When
                battleHudView.hide()
                // Then
                assertThat(battleHudView.children).isEmpty()
            }
    }
}
