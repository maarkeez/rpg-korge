package com.mkz.rpg.screen

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import korlibs.image.bitmap.Bitmap
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
                val tiles =
                    listOf(
                        listOf("void", "sand"),
                    )
                val battlefield =
                    battlefield(
                        rows = 1,
                        columns = 2,
                        tiles = tiles,
                        terrainTransitionRules = setOf(BattlefieldMother.terrainTransitionRule(fromTerrainId = "sand", toTerrainId = "void")),
                    ).toDto()
                // When
                battlefieldView.displayBattlefield(battlefield)
                // Then
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-1") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.TERRAIN)).isNotNull
            }
    }

    @Nested
    inner class TerrainTileBitmap {
        @Test
        fun `should return the pure terrain tile of the strip when the tile has no terrain transition`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val transitionStrip = resourcesVfs["terrain/transitions/sand_to_void.png"].readBitmap()
                // When
                val tileBitmap = battlefieldView.terrainTileBitmap(terrainId = "sand", terrainTransition = null)
                // Then
                assertThat(tileBitmap.width).isEqualTo(BattlefieldView.TILE_PIXEL_SIZE)
                assertThat(tileBitmap.height).isEqualTo(BattlefieldView.TILE_PIXEL_SIZE)
                assertThat(matchesWangTile(tileBitmap, transitionStrip, stripIndex = 0)).isTrue
            }

        @Test
        fun `should return the pure target terrain tile of the strip when the target terrain tile has no terrain transition`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val transitionStrip = resourcesVfs["terrain/transitions/sand_to_void.png"].readBitmap()
                // When
                val tileBitmap = battlefieldView.terrainTileBitmap(terrainId = "void", terrainTransition = null)
                // Then
                assertThat(matchesWangTile(tileBitmap, transitionStrip, stripIndex = 15)).isTrue
            }

        @Test
        fun `should return the transition tile of the strip when the tile has a terrain transition`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val transitionStrip = resourcesVfs["terrain/transitions/sand_to_void.png"].readBitmap()
                // When
                val tileBitmap =
                    battlefieldView
                        .terrainTileBitmap(
                            terrainId = "sand",
                            terrainTransition = Battlefield.Dto.TerrainTransitionDto(fromTerrainId = "sand", toTerrainId = "void", wangIndex = 9),
                        )
                // Then
                assertThat(tileBitmap.width).isEqualTo(BattlefieldView.TILE_PIXEL_SIZE)
                assertThat(tileBitmap.height).isEqualTo(BattlefieldView.TILE_PIXEL_SIZE)
                assertThat(matchesWangTile(tileBitmap, transitionStrip, stripIndex = 9)).isTrue
            }

        @Test
        fun `should return the last tile of the strip when the terrain transition has all target terrain edges`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val transitionStrip = resourcesVfs["terrain/transitions/sand_to_void.png"].readBitmap()
                // When
                val tileBitmap =
                    battlefieldView
                        .terrainTileBitmap(
                            terrainId = "sand",
                            terrainTransition = Battlefield.Dto.TerrainTransitionDto(fromTerrainId = "sand", toTerrainId = "void", wangIndex = 15),
                        )
                // Then
                assertThat(matchesWangTile(tileBitmap, transitionStrip, stripIndex = 15)).isTrue
            }

        @Test
        fun `should throw when the terrain bitmap is not found and the tile has no terrain transition`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                // When
                val result = runCatching { battlefieldView.terrainTileBitmap(terrainId = "forest", terrainTransition = null) }
                // Then
                assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
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
        stripIndex: Int,
    ): Boolean {
        val tileSize = BattlefieldView.TILE_PIXEL_SIZE
        val stripOffset = stripIndex * tileSize
        for (y in 0 until tileSize) {
            for (x in 0 until tileSize) {
                if (tile.getRgba(x, y) != transitionStrip.getRgba(stripOffset + x, y)) return false
            }
        }
        return true
    }
}
