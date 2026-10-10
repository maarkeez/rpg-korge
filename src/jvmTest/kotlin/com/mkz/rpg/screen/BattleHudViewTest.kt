package com.mkz.rpg.screen

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.SolidRect
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class BattleHudViewTest : ViewsForTesting() {
    @Nested
    inner class DisplayBattleUnitInfoView {
        @Test
        fun `should display battle unit info view when is displayed`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView()
                val attackPreviewView = AttackPreviewView()
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
                val battleUnitInfoView = BattleUnitInfoView()
                val attackPreviewView = AttackPreviewView()
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
                val battleUnitInfoView = BattleUnitInfoView()
                val attackPreviewView = AttackPreviewView()
                val battleHudView = BattleHudView(Size(width = 400.00, height = 200.00), battleUnitInfoView, attackPreviewView)
                battleHudView.displayBattleUnitInfoView()
                // When
                battleHudView.hide()
                // Then
                assertThat(battleHudView.children).isEmpty()
            }
    }

    @Nested
    inner class SheetTouches {
        @Test
        fun `should not select a tile when the sheet area is tapped while a battle unit info is displayed`() =
            viewsTest {
                // Given
                val delegate = mock<BattlefieldView.Delegate>()
                val battlefieldView = BattlefieldView(viewportSize = Size(390, 800))
                battlefieldView.loadAssets()
                battlefieldView.setDelegate(delegate)
                battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
                addChild(battlefieldView)
                val panel = SolidRect(Size(390, 230), Colors.BLACK)
                panel.y = 500.0
                addChild(panel)
                val battleHudView = BattleHudView(Size(390, 230), BattleUnitInfoView(), AttackPreviewView(), panel)
                battleHudView.y = 500.0
                addChild(battleHudView)
                battleHudView.displayBattleUnitInfoView()
                // When
                mouseMoveAndClickTo(300, 700)
                // Then
                verify(delegate, never()).tileSelected(any(), any())
            }

        @Test
        fun `should select a tile when the sheet area is tapped while the sheet is hidden`() =
            viewsTest {
                // Given
                val delegate = mock<BattlefieldView.Delegate>()
                val battlefieldView = BattlefieldView(viewportSize = Size(390, 800))
                battlefieldView.loadAssets()
                battlefieldView.setDelegate(delegate)
                battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
                addChild(battlefieldView)
                val panel = SolidRect(Size(390, 230), Colors.BLACK)
                panel.y = 500.0
                addChild(panel)
                val battleHudView = BattleHudView(Size(390, 230), BattleUnitInfoView(), AttackPreviewView(), panel)
                battleHudView.y = 500.0
                addChild(battleHudView)
                battleHudView.displayBattleUnitInfoView()
                battleHudView.hide()
                // When
                mouseMoveAndClickTo(300, 700)
                // Then
                verify(delegate).tileSelected(row = 14, column = 6)
            }
    }
}
