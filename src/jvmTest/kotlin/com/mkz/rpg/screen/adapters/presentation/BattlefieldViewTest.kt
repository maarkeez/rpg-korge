package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import korlibs.image.bitmap.Bitmap
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.tests.ViewsForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class BattlefieldViewTest : ViewsForTesting() {
    private companion object {
        // The battlefield scroll animation lasts 300ms and each test frame 10ms.
        const val SCROLL_ANIMATION_FRAMES = 40

        // A moving battle unit stops 150ms on each tile and each test frame lasts 10ms.
        const val MOVE_STEP_FRAMES = 15
        const val MAX_MOVE_FRAMES = 1_000
    }

    @Nested
    inner class DisplayBattlefield {
        @Test
        fun `should not scroll the battlefield when is clicked and is smaller than the viewport`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                addChild(battlefieldView)
                // When
                clickTile(battlefieldView, row = 0, column = 0)
                // Then
                repeat(SCROLL_ANIMATION_FRAMES) { delayFrame() }
                assertThat(battlefieldView.scrollOffset()).isEqualTo(0.0 to 0.0)
            }

        @Test
        fun `should display battlefield tiles when battlefield is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                val battlefield = battlefield(rows = 2, columns = 3).toDto()
                // When
                battlefieldView.displayBattlefield(battlefield)
                // Then
                assertThat(battlefieldView.tileCount()).isEqualTo(6)
            }

        @Test
        fun `should display terrain on each tile when battlefield is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                val battlefield = battlefield(rows = 1, columns = 1).toDto()
                // When
                battlefieldView.displayBattlefield(battlefield)
                // Then
                assertThat(battlefieldView.hasTerrainAt(row = 0, column = 0)).isTrue
            }

        @Test
        fun `should display terrain transition tile on tile adjacent to void when battlefield is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
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
                assertThat(battlefieldView.hasTerrainAt(row = 0, column = 1)).isTrue
            }
    }

    @Nested
    inner class TerrainTileBitmap {
        @Test
        fun `should return the pure terrain tile of the strip when the tile has no terrain transition`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
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
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
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
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
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
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
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
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                // When
                val result = runCatching { battlefieldView.terrainTileBitmap(terrainId = "forest", terrainTransition = null) }
                // Then
                assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
            }
    }

    @Nested
    inner class DisplayBattleUnit {
        @Test
        fun `should display knight battle unit on tile when is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
                // Then
                assertThat(battlefieldView.hasBattleUnitAt(row = 0, column = 0)).isTrue
            }

        @Test
        fun `should display rat battle unit on tile when is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "rat")
                // Then
                assertThat(battlefieldView.hasBattleUnitAt(row = 0, column = 0)).isTrue
            }

        @Test
        fun `should not display battle unit on tile when the unit has no image`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "dragon")
                // Then
                assertThat(battlefieldView.hasBattleUnitAt(row = 0, column = 0)).isFalse
            }
    }

    @Nested
    inner class DisplayPotentialMovement {
        @Test
        fun `should display selection on tile when potential movement is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayPotentialMovement(row = 0, column = 0)
                // Then
                assertThat(battlefieldView.hasSelectionAt(row = 0, column = 0)).isTrue
            }
    }

    @Nested
    inner class DisplayPotentialCast {
        @Test
        fun `should display selection on tile when potential cast is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayPotentialCast(row = 0, column = 0)
                // Then
                assertThat(battlefieldView.hasSelectionAt(row = 0, column = 0)).isTrue
            }

        @Test
        fun `should not display duplicated selection when potential cast is displayed twice`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                battlefieldView.displayPotentialCast(row = 0, column = 0)
                // When
                battlefieldView.displayPotentialCast(row = 0, column = 0)
                // Then
                val selectionCount = battlefieldView.selectionCountAt(row = 0, column = 0)
                assertThat(selectionCount).isEqualTo(1)
            }
    }

    @Nested
    inner class DisplayTileSelection {
        @Test
        fun `should display selection on tile when tile selection is displayed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                // When
                battlefieldView.displayTileSelection(row = 0, column = 0)
                // Then
                assertThat(battlefieldView.hasSelectionAt(row = 0, column = 0)).isTrue
            }

        @Test
        fun `should replace selection when tile selection is displayed over potential cast`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                battlefieldView.displayPotentialCast(row = 0, column = 0)
                // When
                battlefieldView.displayTileSelection(row = 0, column = 0)
                // Then
                val selectionCount = battlefieldView.selectionCountAt(row = 0, column = 0)
                assertThat(selectionCount).isEqualTo(1)
            }
    }

    @Nested
    inner class ResetTiles {
        @Test
        fun `should remove selections from tiles when tiles are reset`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 2).toDto())
                battlefieldView.displayPotentialMovement(row = 0, column = 0)
                battlefieldView.displayTileSelection(row = 0, column = 1)
                // When
                battlefieldView.resetTiles()
                // Then
                assertThat(battlefieldView.tilesWithSelectionCount()).isEqualTo(0)
            }
    }

    @Nested
    inner class RemoveBattleUnit {
        @Test
        fun `should remove battle unit from tile when is removed`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
                // When
                battlefieldView.removeBattleUnit(row = 0, column = 0)
                // Then
                assertThat(battlefieldView.hasBattleUnitAt(row = 0, column = 0)).isFalse
            }
    }

    @Nested
    inner class MoveBattleUnit {
        private val path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1), Battlefield.Dto.PositionDto(row = 0, column = 2))

        @Test
        fun `should display the battle unit on each tile of the path in order when it moves`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 3).toDto())
                battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
                addChild(battlefieldView)
                val displayedPositions = mutableListOf(battlefieldView.battleUnitPositions())
                // When
                val moving = startMoving(battlefieldView, path)
                while (!moving.isCompleted && displayedPositions.size < MAX_MOVE_FRAMES) {
                    displayedPositions += battlefieldView.battleUnitPositions()
                    nextFrame()
                }
                // Then
                assertThat(displayedPositions.distinct()).containsExactly(listOf(0 to 0), listOf(0 to 1), listOf(0 to 2))
                assertThat(displayedPositions.zipWithNext().count { (previous, next) -> previous != next }).isEqualTo(2)
            }

        @Test
        fun `should stop on each tile of the path when the battle unit moves`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 3).toDto())
                battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
                addChild(battlefieldView)
                // When
                startMoving(battlefieldView, path)
                repeat(MOVE_STEP_FRAMES - 2) { nextFrame() }
                // Then
                assertThat(battlefieldView.battleUnitPositions()).containsExactly(0 to 1)
            }

        @Test
        fun `should scroll the battlefield to keep the battle unit visible when it moves out of the viewport`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 16).toDto())
                battlefieldView.displayBattleUnit(row = 0, column = 5, unitId = "rat")
                addChild(battlefieldView)
                val pathOutOfTheViewport = (6..9).map { column -> Battlefield.Dto.PositionDto(row = 0, column = column) }
                require(!battlefieldView.isTileVisible(row = 0, column = 9))
                // When
                awaitCompletion(startMoving(battlefieldView, pathOutOfTheViewport))
                // Then
                assertThat(battlefieldView.battleUnitPositions()).containsExactly(0 to 9)
                assertThat(battlefieldView.isTileVisible(row = 0, column = 9)).isTrue
            }

        @Test
        fun `should not scroll the battlefield when the battle unit moves and the battlefield is smaller than the viewport`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 3).toDto())
                battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
                addChild(battlefieldView)
                // When
                awaitCompletion(startMoving(battlefieldView, path))
                // Then
                assertThat(battlefieldView.scrollOffset()).isEqualTo(0.0 to 0.0)
            }

        private suspend fun startMoving(
            battlefieldView: BattlefieldView,
            path: List<Battlefield.Dto.PositionDto>,
        ): Job {
            val origin = battlefieldView.battleUnitPositions().single()
            return CoroutineScope(currentCoroutineContext()).launch(start = CoroutineStart.UNDISPATCHED) {
                battlefieldView.moveBattleUnit(fromRow = origin.first, fromColumn = origin.second, path = path, unitId = "knight")
            }
        }

        // The battle unit moves in another coroutine, which only runs when this one yields.
        private suspend fun nextFrame() {
            delayFrame()
            yield()
        }

        private suspend fun awaitCompletion(job: Job) {
            var frames = 0
            while (!job.isCompleted && frames++ < MAX_MOVE_FRAMES) nextFrame()
        }
    }

    @Nested
    inner class TileClick {
        @Test
        fun `should notify delegate with tile position when tile is clicked`() =
            viewsTest {
                // Given
                val battlefieldView = BattlefieldView(loadedResourcesLoader())
                val delegate = mock<BattlefieldView.Delegate>()
                battlefieldView.setDelegate(delegate)
                battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
                addChild(battlefieldView)
                // When
                clickTile(battlefieldView, row = 0, column = 0)
                // Then
                verify(delegate).tileSelected(row = 0, column = 0)
            }
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
