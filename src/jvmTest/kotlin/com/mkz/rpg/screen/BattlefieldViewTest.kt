package com.mkz.rpg.screen

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.adapters.presentation.snapToArtPixel
import korlibs.image.bitmap.Bitmap
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.descendantsWith
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
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

    @Nested
    inner class DisplayUnitOverlay {
        @Test
        fun `should display the overlay on the tile when the unit overlay is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayUnitOverlay(row = 0, column = 0, state = overlayState())
                // Then
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.UNIT_OVERLAY)).isNotNull
            }

        @Test
        fun `should not duplicate the overlay when the unit overlay is displayed twice`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                battlefieldView.displayUnitOverlay(row = 0, column = 0, state = overlayState())
                // When
                battlefieldView.displayUnitOverlay(row = 0, column = 0, state = overlayState(remainingHealthPoints = 5))
                // Then
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.children.count { it.name == BattlefieldView.UNIT_OVERLAY }).isEqualTo(1)
                assertThat((tileButton.findViewByName(BattlefieldView.UNIT_OVERLAY) as UnitOverlayView).hpFraction).isEqualTo(0.5)
            }

        @Test
        fun `should remove the overlay when the battle unit is removed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                battlefieldView.displayKnightBattleUnit(row = 0, column = 0)
                battlefieldView.displayUnitOverlay(row = 0, column = 0, state = overlayState())
                // When
                battlefieldView.removeBattleUnit(row = 0, column = 0)
                // Then
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.UNIT_OVERLAY)).isNull()
            }
    }

    @Nested
    inner class DisplayUnitSelection {
        @Test
        fun `should display corner brackets without inspect glyph when an ally is selected`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayUnitSelection(row = 0, column = 0, isEnemy = false)
                // Then
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.SELECTION)).isNotNull
                assertThat(tileButton.descendantsWith { it.name == BattlefieldView.INSPECT_GLYPH }).isEmpty()
            }

        @Test
        fun `should display the inspect glyph when an enemy is selected`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayUnitSelection(row = 0, column = 0, isEnemy = true)
                // Then
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.descendantsWith { it.name == BattlefieldView.INSPECT_GLYPH }).hasSize(1)
            }

        @Test
        fun `should replace the selection when the unit selection is displayed over a movement range tile`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                battlefieldView.displayPotentialMovement(row = 0, column = 0)
                // When
                battlefieldView.displayUnitSelection(row = 0, column = 0, isEnemy = false)
                // Then
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.children.count { it.name == BattlefieldView.SELECTION }).isEqualTo(1)
            }

        @Test
        fun `should remove the brackets when tiles are reset`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                battlefieldView.displayUnitSelection(row = 0, column = 0, isEnemy = true)
                // When
                battlefieldView.resetTiles()
                // Then
                val tileButton = getBattlefieldGrid(battlefieldView).findViewByName("row-0-column-0") as UIButton
                assertThat(tileButton.findViewByName(BattlefieldView.SELECTION)).isNull()
            }
    }

    @Nested
    inner class Drag {
        @Test
        fun `should not select a tile when the pointer is dragged beyond the tap threshold`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val delegate = mock<BattlefieldView.Delegate>()
                battlefieldView.setDelegate(delegate)
                battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
                addChild(battlefieldView)
                // When
                mouseMoveTo(100, 100)
                mouseDown()
                mouseMoveTo(100, 106)
                mouseMoveTo(100, 112)
                mouseUp()
                // Then
                verify(delegate, never()).tileSelected(any(), any())
            }

        @Test
        fun `should select a tile when the pointer moves less than the tap threshold`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                val delegate = mock<BattlefieldView.Delegate>()
                battlefieldView.setDelegate(delegate)
                battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
                addChild(battlefieldView)
                // When
                mouseMoveTo(100, 100)
                mouseDown()
                mouseMoveTo(102, 103)
                mouseUp()
                // Then
                verify(delegate).tileSelected(row = 2, column = 2)
            }

        @Test
        fun `should keep the scroll offset on art pixel multiples when the pointer is dragged`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
                addChild(battlefieldView)
                // When
                mouseMoveTo(200, 200)
                mouseDown()
                mouseMoveTo(177, 183)
                mouseMoveTo(151, 161)
                mouseUp()
                // Then
                val battlefieldGrid = getBattlefieldGrid(battlefieldView)
                assertThat(battlefieldGrid.x).isNotZero().isEqualTo(snapToArtPixel(battlefieldGrid.x))
                assertThat(battlefieldGrid.y).isNotZero().isEqualTo(snapToArtPixel(battlefieldGrid.y))
            }

        @Test
        fun `should not scroll beyond the map bounds when the pointer is dragged past the map edge`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
                addChild(battlefieldView)
                // When
                mouseMoveTo(200, 200)
                mouseDown()
                mouseMoveTo(-3000, -3000)
                mouseUp()
                // Then
                val battlefieldGrid = getBattlefieldGrid(battlefieldView)
                assertThat(battlefieldGrid.x).isGreaterThanOrEqualTo(-(16.0 * BattlefieldView.TILE_SIZE - BattlefieldView.VIEWPORT_WIDTH))
                assertThat(battlefieldGrid.y).isGreaterThanOrEqualTo(-(16.0 * BattlefieldView.TILE_SIZE - BattlefieldView.VIEWPORT_HEIGHT))
            }
    }

    @Nested
    inner class Viewport {
        @Test
        fun `should use the injected viewport size when it is provided`() =
            viewsTest {
                // Given
                val viewportSize = Size(390, 800)
                // When
                val battlefieldView = BattlefieldView(viewportSize = viewportSize)
                // Then
                assertThat(battlefieldView.width).isEqualTo(390.0)
                assertThat(battlefieldView.height).isEqualTo(800.0)
            }
    }

    @Nested
    inner class CenterOn {
        @Test
        fun `should scroll so the tile is centered on an art pixel multiple when it is centered`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
                addChild(battlefieldView)
                // When
                battlefieldView.centerOn(row = 8, column = 8)
                // Then
                val battlefieldGrid = getBattlefieldGrid(battlefieldView)
                val tileCenterX = battlefieldGrid.x + 8 * BattlefieldView.TILE_SIZE + BattlefieldView.TILE_SIZE / 2.0
                val tileCenterY = battlefieldGrid.y + 8 * BattlefieldView.TILE_SIZE + BattlefieldView.TILE_SIZE / 2.0
                assertThat(tileCenterX).isBetween(BattlefieldView.VIEWPORT_WIDTH / 2.0 - 3, BattlefieldView.VIEWPORT_WIDTH / 2.0 + 3)
                assertThat(tileCenterY).isBetween(BattlefieldView.VIEWPORT_HEIGHT / 2.0 - 3, BattlefieldView.VIEWPORT_HEIGHT / 2.0 + 3)
                assertThat(battlefieldGrid.x).isEqualTo(snapToArtPixel(battlefieldGrid.x))
                assertThat(battlefieldGrid.y).isEqualTo(snapToArtPixel(battlefieldGrid.y))
            }

        @Test
        fun `should clamp the scroll to the map bounds when the tile is near the map corner`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView()
                battlefieldView.loadAssets()
                battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
                addChild(battlefieldView)
                // When
                battlefieldView.centerOn(row = 0, column = 0)
                // Then
                val battlefieldGrid = getBattlefieldGrid(battlefieldView)
                assertThat(battlefieldGrid.x).isEqualTo(0.0)
                assertThat(battlefieldGrid.y).isEqualTo(0.0)
            }
    }

    private fun overlayState(remainingHealthPoints: Int = 10) =
        UnitOverlayState(
            remainingHealthPoints = remainingHealthPoints,
            maximumHealthPoints = 10,
            isEnemy = false,
            onTurnStartedEffectCount = 0,
            onDefeatedEffectCount = 0,
        )

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
