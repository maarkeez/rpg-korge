package com.mkz.rpg.screen

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class BattlefieldViewTest : ViewsForTesting() {
    @Nested
    inner class DisplayBattlefield {
        @Test
        fun `should display battlefield tiles when battlefield is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val battlefield = battlefield(rows = 2, columns = 3).toDto()
                // When
                battlefieldView.displayBattlefield(battlefield)
                // Then
                val battlefieldGrid = battlefieldView.children[0] as Container
                assertThat(battlefieldGrid.children.size).isEqualTo(6)
            }

        @Test
        fun `should display terrain on each tile when battlefield is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val battlefield = battlefield(rows = 1, columns = 1).toDto()
                // When
                battlefieldView.displayBattlefield(battlefield)
                // Then
                val tileButton = battlefieldView.children[0].findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.TERRAIN)).isNotNull
            }
    }

    @Nested
    inner class DisplayKnightBattleUnit {
        @Test
        fun `should display knight battle unit on tile when is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayKnightBattleUnit(row = 0, column = 0)
                // Then
                val tileButton = battlefieldView.children[0].findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.BATTLE_UNIT)).isNotNull
            }
    }

    @Nested
    inner class DisplayRatBattleUnit {
        @Test
        fun `should display rat battle unit on tile when is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayRatBattleUnit(row = 0, column = 0)
                // Then
                val tileButton = battlefieldView.children[0].findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.BATTLE_UNIT)).isNotNull
            }
    }

    @Nested
    inner class DisplayPotentialMovement {
        @Test
        fun `should display selection on tile when potential movement is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayPotentialMovement(row = 0, column = 0)
                // Then
                val tileButton = battlefieldView.children[0].findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.SELECTION)).isNotNull
            }
    }

    @Nested
    inner class DisplayPotentialCast {
        @Test
        fun `should display selection on tile when potential cast is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayPotentialCast(row = 0, column = 0)
                // Then
                val tileButton = battlefieldView.children[0].findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.SELECTION)).isNotNull
            }

        @Test
        fun `should not display duplicated selection when potential cast is displayed twice`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                battlefieldView.displayPotentialCast(row = 0, column = 0)
                // When
                battlefieldView.displayPotentialCast(row = 0, column = 0)
                // Then
                val tileButton = battlefieldView.children[0].findViewByName("row-0-column-0") as UIButton
                val selectionCount = tileButton.children.count { child -> child.name == BattlefieldView.SELECTION }
                assertThat(selectionCount).isEqualTo(1)
            }
    }

    @Nested
    inner class DisplayTileSelection {
        @Test
        fun `should display selection on tile when tile selection is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayTileSelection(row = 0, column = 0)
                // Then
                val tileButton = battlefieldView.children[0].findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.SELECTION)).isNotNull
            }

        @Test
        fun `should replace selection when tile selection is displayed over potential cast`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                battlefieldView.displayPotentialCast(row = 0, column = 0)
                // When
                battlefieldView.displayTileSelection(row = 0, column = 0)
                // Then
                val tileButton = battlefieldView.children[0].findViewByName("row-0-column-0") as UIButton
                val selectionCount = tileButton.children.count { child -> child.name == BattlefieldView.SELECTION }
                assertThat(selectionCount).isEqualTo(1)
            }
    }

    @Nested
    inner class ResetTiles {
        @Test
        fun `should remove selections from tiles when tiles are reset`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 2).toDto())
                battlefieldView.displayPotentialMovement(row = 0, column = 0)
                battlefieldView.displayTileSelection(row = 0, column = 1)
                // When
                battlefieldView.resetTiles()
                // Then
                val battlefieldGrid = battlefieldView.children[0] as Container
                val selections =
                    battlefieldGrid.children.count { tile ->
                        (tile as UIButton).findViewByName(BattlefieldView.SELECTION) != null
                    }
                assertThat(selections).isEqualTo(0)
            }
    }

    @Nested
    inner class RemoveBattleUnit {
        @Test
        fun `should remove battle unit from tile when is removed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                battlefieldView.displayKnightBattleUnit(row = 0, column = 0)
                // When
                battlefieldView.removeBattleUnit(row = 0, column = 0)
                // Then
                val tileButton = battlefieldView.children[0].findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.BATTLE_UNIT)).isNull()
            }
    }

    @Nested
    inner class TileClick {
        @Test
        fun `should notify delegate with tile position when tile is clicked`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val delegate = mock<BattlefieldView.Delegate>()
                battlefieldView.setDelegate(delegate)
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                addChild(battlefieldView)
                val tileButton = battlefieldView.children[0].findViewByName("row-0-column-0") as UIButton
                // When
                tileButton.simulateClick()
                // Then
                verify(delegate).tileSelected(row = 0, column = 0)
            }
    }
}
