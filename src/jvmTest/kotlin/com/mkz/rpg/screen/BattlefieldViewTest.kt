package com.mkz.rpg.screen

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.matchContents
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
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
                val battlefieldGrid = getBattlefieldGrid(battlefieldView)
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
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.TERRAIN)).isNotNull
            }

        @Test
        fun `should display terrain transition tile on tile adjacent to void when battlefield is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val battlefield =
                    battlefield(
                        rows = 1,
                        columns = 2,
                        tiles =
                            listOf(
                                listOf("void", "sand"),
                            ),
                    ).toDto()
                // When
                battlefieldView.displayBattlefield(battlefield)
                // Then
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-1") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.TERRAIN)).isNotNull
            }
    }

    @Nested
    inner class TerrainWangIndex {
        @Test
        fun `should return 0 when tile is not adjacent to void tiles`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                val tiles = battlefield(rows = 3, columns = 3, tiles = List(3) { List(3) { "sand" } }).toDto().tiles
                // When
                val wangIndex = battlefieldView.terrainWangIndex(row = 1, column = 1, tiles = tiles)
                // Then
                assertThat(wangIndex).isEqualTo(0)
            }

        @Test
        fun `should return 15 when tile is adjacent to void tiles on all its sides`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                val tiles =
                    battlefield(
                        rows = 3,
                        columns = 3,
                        tiles =
                            listOf(
                                listOf("void", "void", "void"),
                                listOf("void", "sand", "void"),
                                listOf("void", "void", "void"),
                            ),
                    ).toDto().tiles
                // When
                val wangIndex = battlefieldView.terrainWangIndex(row = 1, column = 1, tiles = tiles)
                // Then
                assertThat(wangIndex).isEqualTo(15)
            }

        @Test
        fun `should return 9 when tile is adjacent to void tiles on north and west`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                val tiles =
                    battlefield(
                        rows = 3,
                        columns = 3,
                        tiles =
                            listOf(
                                listOf("void", "void", "sand"),
                                listOf("void", "sand", "sand"),
                                listOf("sand", "sand", "sand"),
                            ),
                    ).toDto().tiles
                // When
                val wangIndex = battlefieldView.terrainWangIndex(row = 1, column = 1, tiles = tiles)
                // Then
                assertThat(wangIndex).isEqualTo(9)
            }

        @Test
        fun `should not add weight when adjacent position is out of battlefield boundaries`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                val tiles = battlefield(rows = 1, columns = 2, tiles = listOf(listOf("void", "sand"))).toDto().tiles
                // When
                val wangIndex = battlefieldView.terrainWangIndex(row = 0, column = 1, tiles = tiles)
                // Then
                assertThat(wangIndex).isEqualTo(8)
            }
    }

    @Nested
    inner class TerrainTileBitmap {
        @Test
        fun `should return terrain bitmap when wang index has no void edges`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val terrainBitmap = resourcesVfs["terrain/sand.png"].readBitmap()
                // When
                val tileBitmap = battlefieldView.terrainTileBitmap(terrainId = "sand", wangIndex = 0)
                // Then
                assertThat(tileBitmap.matchContents(terrainBitmap)).isTrue
            }

        @Test
        fun `should return void bitmap when wang index has all void edges`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val voidBitmap = resourcesVfs["terrain/void.png"].readBitmap()
                // When
                val tileBitmap = battlefieldView.terrainTileBitmap(terrainId = "sand", wangIndex = 15)
                // Then
                assertThat(tileBitmap.matchContents(voidBitmap)).isTrue
            }

        @Test
        fun `should return transition tile bitmap when wang index has some void edges`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val transitionStrip = resourcesVfs["terrain/transitions/sand_to_void.png"].readBitmap()
                // When
                val tileBitmap = battlefieldView.terrainTileBitmap(terrainId = "sand", wangIndex = 9)
                // Then
                assertThat(tileBitmap.width).isEqualTo(BattlefieldView.TILE_PIXEL_SIZE)
                assertThat(tileBitmap.height).isEqualTo(BattlefieldView.TILE_PIXEL_SIZE)
                assertThat(matchesWangTile(tileBitmap, transitionStrip, wangIndex = 9)).isTrue
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
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
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
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
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
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
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
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
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
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
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
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
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
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
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
                val battlefieldGrid = getBattlefieldGrid(battlefieldView)
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
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
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
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
                // When
                tileButton.simulateClick()
                // Then
                verify(delegate).tileSelected(row = 0, column = 0)
            }
    }

    private fun getBattlefieldGrid(battlefieldView: BattlefieldView): Container {
        val viewport = battlefieldView.children[0] as Container
        val battlefieldGrid = viewport.children[0] as Container
        return battlefieldGrid
    }

    private fun matchesWangTile(
        tile: Bitmap,
        transitionStrip: Bitmap,
        wangIndex: Int,
    ): Boolean {
        val tileSize = BattlefieldView.TILE_PIXEL_SIZE
        val stripOffset = (wangIndex - 1) * tileSize
        for (y in 0 until tileSize) {
            for (x in 0 until tileSize) {
                if (tile.getRgba(x, y) != transitionStrip.getRgba(stripOffset + x, y)) return false
            }
        }
        return true
    }
}
