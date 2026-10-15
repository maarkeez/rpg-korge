package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.awaitCompletion
import com.mkz.rpg.shared.adapters.presentation.nextFrame
import com.mkz.rpg.shared.adapters.presentation.sampleUntilCompletion
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.shared.adapters.presentation.startAnimation
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import korlibs.image.bitmap.Bitmap
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.tests.ViewsForTesting
import kotlinx.coroutines.Job
import kotlin.test.Test

class BattlefieldViewTest : ViewsForTesting() {
    private companion object {
        // The battlefield scroll animation lasts 300ms and each test frame 10ms.
        const val SCROLL_ANIMATION_FRAMES = 40

        // A moving battle unit stops 150ms on each tile and each test frame lasts 10ms.
        const val MOVE_STEP_FRAMES = 15
    }

    @Test
    fun `DisplayBattlefield should not scroll the battlefield when is clicked and is smaller than the viewport`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            addChild(battlefieldView)
            // When
            clickTile(battlefieldView, row = 0, column = 0)
            // Then
            repeat(SCROLL_ANIMATION_FRAMES) { delayFrame() }
            battlefieldView.scrollOffset() shouldBe (0.0 to 0.0)
        }

    @Test
    fun `DisplayBattlefield should display battlefield tiles when battlefield is displayed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            val battlefield = battlefield(rows = 2, columns = 3).toDto()
            // When
            battlefieldView.displayBattlefield(battlefield)
            // Then
            battlefieldView.tileCount() shouldBe 6
        }

    @Test
    fun `DisplayBattlefield should display terrain on each tile when battlefield is displayed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            val battlefield = battlefield(rows = 1, columns = 1).toDto()
            // When
            battlefieldView.displayBattlefield(battlefield)
            // Then
            battlefieldView.hasTerrainAt(row = 0, column = 0).shouldBeTrue()
        }

    @Test
    fun `DisplayBattlefield should display terrain transition tile on tile adjacent to void when battlefield is displayed`() =
        sharedViewsTest {
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
            battlefieldView.hasTerrainAt(row = 0, column = 1).shouldBeTrue()
        }

    @Test
    fun `TerrainTileBitmap should return the pure terrain tile of the strip when the tile has no terrain transition`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            val transitionStrip = resourcesVfs["terrain/transitions/sand_to_void.png"].readBitmap()
            // When
            val tileBitmap = battlefieldView.terrainTileBitmap(terrainId = "sand", terrainTransition = null)
            // Then
            tileBitmap.width shouldBe BattlefieldView.TILE_PIXEL_SIZE
            tileBitmap.height shouldBe BattlefieldView.TILE_PIXEL_SIZE
            matchesWangTile(tileBitmap, transitionStrip, stripIndex = 0).shouldBeTrue()
        }

    @Test
    fun `TerrainTileBitmap should return the pure target terrain tile of the strip when the target terrain tile has no terrain transition`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            val transitionStrip = resourcesVfs["terrain/transitions/sand_to_void.png"].readBitmap()
            // When
            val tileBitmap = battlefieldView.terrainTileBitmap(terrainId = "void", terrainTransition = null)
            // Then
            matchesWangTile(tileBitmap, transitionStrip, stripIndex = 15).shouldBeTrue()
        }

    @Test
    fun `TerrainTileBitmap should return the transition tile of the strip when the tile has a terrain transition`() =
        sharedViewsTest {
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
            tileBitmap.width shouldBe BattlefieldView.TILE_PIXEL_SIZE
            tileBitmap.height shouldBe BattlefieldView.TILE_PIXEL_SIZE
            matchesWangTile(tileBitmap, transitionStrip, stripIndex = 9).shouldBeTrue()
        }

    @Test
    fun `TerrainTileBitmap should return the last tile of the strip when the terrain transition has all target terrain edges`() =
        sharedViewsTest {
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
            matchesWangTile(tileBitmap, transitionStrip, stripIndex = 15).shouldBeTrue()
        }

    @Test
    fun `TerrainTileBitmap should throw when the terrain bitmap is not found and the tile has no terrain transition`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            // When
            val result = runCatching { battlefieldView.terrainTileBitmap(terrainId = "forest", terrainTransition = null) }
            // Then
            result.exceptionOrNull().shouldBeInstanceOf<IllegalStateException>()
        }

    @Test
    fun `DisplayBattleUnit should display knight battle unit on tile when is displayed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            // When
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            // Then
            battlefieldView.hasBattleUnitAt(row = 0, column = 0).shouldBeTrue()
        }

    @Test
    fun `DisplayBattleUnit should display rat battle unit on tile when is displayed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            // When
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "rat")
            // Then
            battlefieldView.hasBattleUnitAt(row = 0, column = 0).shouldBeTrue()
        }

    @Test
    fun `DisplayBattleUnit should not display battle unit on tile when the unit has no image`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            // When
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "dragon")
            // Then
            battlefieldView.hasBattleUnitAt(row = 0, column = 0).shouldBeFalse()
        }

    @Test
    fun `DisplayPotentialMovement should display selection on tile when potential movement is displayed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            // When
            battlefieldView.displayPotentialMovement(row = 0, column = 0)
            // Then
            battlefieldView.hasSelectionAt(row = 0, column = 0).shouldBeTrue()
        }

    @Test
    fun `DisplayPotentialCast should display selection on tile when potential cast is displayed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            // When
            battlefieldView.displayPotentialCast(row = 0, column = 0)
            // Then
            battlefieldView.hasSelectionAt(row = 0, column = 0).shouldBeTrue()
        }

    @Test
    fun `DisplayPotentialCast should not display duplicated selection when potential cast is displayed twice`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayPotentialCast(row = 0, column = 0)
            // When
            battlefieldView.displayPotentialCast(row = 0, column = 0)
            // Then
            val selectionCount = battlefieldView.selectionCountAt(row = 0, column = 0)
            selectionCount shouldBe 1
        }

    @Test
    fun `DisplayTileSelection should display selection on tile when tile selection is displayed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            // When
            battlefieldView.displayTileSelection(row = 0, column = 0)
            // Then
            battlefieldView.hasSelectionAt(row = 0, column = 0).shouldBeTrue()
        }

    @Test
    fun `DisplayTileSelection should replace selection when tile selection is displayed over potential cast`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayPotentialCast(row = 0, column = 0)
            // When
            battlefieldView.displayTileSelection(row = 0, column = 0)
            // Then
            val selectionCount = battlefieldView.selectionCountAt(row = 0, column = 0)
            selectionCount shouldBe 1
        }

    @Test
    fun `ResetTiles should remove selections from tiles when tiles are reset`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 2).toDto())
            battlefieldView.displayPotentialMovement(row = 0, column = 0)
            battlefieldView.displayTileSelection(row = 0, column = 1)
            // When
            battlefieldView.resetTiles()
            // Then
            battlefieldView.tilesWithSelectionCount() shouldBe 0
        }

    @Test
    fun `DimTilesExcept should dim every tile but the given ones when tiles are dimmed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 2, columns = 2).toDto())
            // When
            battlefieldView.dimTilesExcept(setOf(Battlefield.Dto.PositionDto(row = 0, column = 0), Battlefield.Dto.PositionDto(row = 1, column = 1)))
            // Then
            battlefieldView.isDimmedAt(row = 0, column = 0).shouldBeFalse()
            battlefieldView.isDimmedAt(row = 1, column = 1).shouldBeFalse()
            battlefieldView.isDimmedAt(row = 0, column = 1).shouldBeTrue()
            battlefieldView.isDimmedAt(row = 1, column = 0).shouldBeTrue()
        }

    @Test
    fun `DimTilesExcept should dim the battle unit on a tile that is dimmed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 2).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 1, unitId = "rat")
            // When
            battlefieldView.dimTilesExcept(setOf(Battlefield.Dto.PositionDto(row = 0, column = 0)))
            // Then
            battlefieldView.isBattleUnitDimmedAt(row = 0, column = 1).shouldBeTrue()
        }

    @Test
    fun `DimTilesExcept should not dim a tile twice when tiles are dimmed twice`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 2).toDto())
            battlefieldView.dimTilesExcept(setOf(Battlefield.Dto.PositionDto(row = 0, column = 0)))
            // When
            battlefieldView.dimTilesExcept(setOf(Battlefield.Dto.PositionDto(row = 0, column = 0)))
            battlefieldView.resetTiles()
            // Then
            battlefieldView.isDimmedAt(row = 0, column = 1).shouldBeFalse()
        }

    @Test
    fun `DimTilesExcept should remove the mask from every tile when tiles are reset`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 2, columns = 2).toDto())
            battlefieldView.dimTilesExcept(emptySet())
            // When
            battlefieldView.resetTiles()
            // Then
            battlefieldView.dimmedTilesCount() shouldBe 0
        }

    @Test
    fun `RemoveBattleUnit should remove battle unit from tile when is removed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            // When
            battlefieldView.removeBattleUnit(row = 0, column = 0)
            // Then
            battlefieldView.hasBattleUnitAt(row = 0, column = 0).shouldBeFalse()
        }

    @Test
    fun `DisplayLoot should display the item icon below the battle unit when one item lies on the tile`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val battlefieldView = BattlefieldView(resourcesLoader)
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            // When
            battlefieldView.displayLoot(row = 0, column = 0, lootableItemIds = listOf("health-potion"))
            // Then
            battlefieldView.lootBitmapAt(row = 0, column = 0) shouldBe resourcesLoader.bitmap("lootableItem/health-potion.png")
            battlefieldView.isLootRightAboveTerrainAt(row = 0, column = 0).shouldBeTrue()
            battlefieldView.hasBattleUnitAt(row = 0, column = 0).shouldBeTrue()
        }

    @Test
    fun `DisplayLoot should display the chest when several items lie on the tile`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val battlefieldView = BattlefieldView(resourcesLoader)
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayLoot(row = 0, column = 0, lootableItemIds = listOf("health-potion"))
            // When
            battlefieldView.displayLoot(row = 0, column = 0, lootableItemIds = listOf("health-potion", "hammer"))
            // Then
            battlefieldView.lootBitmapAt(row = 0, column = 0) shouldBe resourcesLoader.bitmap("battlefield/chest.png")
        }

    @Test
    fun `DisplayLoot should remove the loot when no item lies on the tile`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayLoot(row = 0, column = 0, lootableItemIds = listOf("health-potion", "hammer"))
            // When
            battlefieldView.displayLoot(row = 0, column = 0, lootableItemIds = emptyList())
            // Then
            battlefieldView.lootBitmapAt(row = 0, column = 0).shouldBeNull()
        }

    private val path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1), Battlefield.Dto.PositionDto(row = 0, column = 2))

    @Test
    fun `MoveBattleUnit should display the battle unit on each tile of the path in order when it moves`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 3).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            val positionsBeforeMoving = battlefieldView.battleUnitPositions()
            // When
            val moving = startMoving(battlefieldView, path)
            val displayedPositions = listOf(positionsBeforeMoving) + sampleUntilCompletion(moving) { battlefieldView.battleUnitPositions() }
            // Then
            displayedPositions.distinct().shouldContainExactly(listOf(0 to 0), listOf(0 to 1), listOf(0 to 2))
            displayedPositions.zipWithNext().count { (previous, next) -> previous != next } shouldBe 2
        }

    @Test
    fun `MoveBattleUnit should stop on each tile of the path when the battle unit moves`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 3).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            // When
            startMoving(battlefieldView, path)
            repeat(MOVE_STEP_FRAMES - 2) { nextFrame() }
            // Then
            battlefieldView.battleUnitPositions().shouldContainExactly(0 to 1)
        }

    @Test
    fun `MoveBattleUnit should scroll the battlefield to keep the battle unit visible when it moves out of the viewport`() =
        sharedViewsTest {
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
            battlefieldView.battleUnitPositions().shouldContainExactly(0 to 9)
            battlefieldView.isTileVisible(row = 0, column = 9).shouldBeTrue()
        }

    @Test
    fun `MoveBattleUnit should not scroll the battlefield when the battle unit moves and the battlefield is smaller than the viewport`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 3).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            // When
            awaitCompletion(startMoving(battlefieldView, path))
            // Then
            battlefieldView.scrollOffset() shouldBe (0.0 to 0.0)
        }

    private suspend fun startMoving(
        battlefieldView: BattlefieldView,
        path: List<Battlefield.Dto.PositionDto>,
    ): Job {
        val origin = battlefieldView.battleUnitPositions().single()
        return startAnimation {
            battlefieldView.moveBattleUnit(fromRow = origin.first, fromColumn = origin.second, path = path, unitId = "knight")
        }
    }

    @Test
    fun `FlashBattleUnit should blink the battle unit when it is flashed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            // When
            val flashing = startFlashing(battlefieldView, row = 0, column = 0, unitId = "knight")
            val flashingFrames = sampleUntilCompletion(flashing) { battlefieldView.isFlashingAt(row = 0, column = 0) }
            // Then
            val blinks = flashingFrames.zipWithNext().count { (previous, next) -> !previous && next } + if (flashingFrames.first()) 1 else 0
            blinks shouldBe BattlefieldView.FLASH_COUNT
            battlefieldView.isFlashingAt(row = 0, column = 0).shouldBeFalse()
            battlefieldView.hasBattleUnitAt(row = 0, column = 0).shouldBeTrue()
        }

    @Test
    fun `FlashBattleUnit should scroll the battlefield to the battle unit when it is flashed out of the viewport`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 12, column = 13, unitId = "rat")
            addChild(battlefieldView)
            require(!battlefieldView.isTileVisible(row = 12, column = 13))
            // When
            awaitCompletion(startFlashing(battlefieldView, row = 12, column = 13, unitId = "rat"))
            // Then
            battlefieldView.isTileVisible(row = 12, column = 13).shouldBeTrue()
        }

    @Test
    fun `FlashBattleUnit should not blink when the unit has no image`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            addChild(battlefieldView)
            // When
            val flashing = startFlashing(battlefieldView, row = 0, column = 0, unitId = "dragon")
            // Then
            flashing.isCompleted.shouldBeTrue()
            battlefieldView.isFlashingAt(row = 0, column = 0).shouldBeFalse()
        }

    private suspend fun startFlashing(
        battlefieldView: BattlefieldView,
        row: Int,
        column: Int,
        unitId: String,
    ): Job = startAnimation { battlefieldView.flashBattleUnit(row = row, column = column, unitId = unitId) }

    @Test
    fun `Tint should tint the battlefield with the colour and fade it out when it is tinted`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            addChild(battlefieldView)
            // When
            val tinting = startAnimation { battlefieldView.tint(BattleInfoView.CPU_TURN_COLOR) }
            val tints = sampleUntilCompletion(tinting) { battlefieldView.tintColor() to battlefieldView.tintAlpha() }
            // Then
            tints.mapNotNull { (color, _) -> color }.distinct().shouldContainExactly(BattleInfoView.CPU_TURN_COLOR)
            tints.maxOf { (_, alpha) -> alpha } shouldBe (BattlefieldView.TINT_ALPHA plusOrMinus 0.05)
            battlefieldView.tintColor().shouldBeNull()
        }

    @Test
    fun `Tint should not scroll the battlefield when it is tinted`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            addChild(battlefieldView)
            // When
            awaitCompletion(startAnimation { battlefieldView.tint(BattleInfoView.HUMAN_TURN_COLOR) })
            // Then
            battlefieldView.scrollOffset() shouldBe (0.0 to 0.0)
        }

    @Test
    fun `TileClick should notify delegate with tile position when tile is clicked`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            val selectedTiles = mutableListOf<Pair<Int, Int>>()
            battlefieldView.setDelegate(
                object : BattlefieldView.Delegate {
                    override fun tileSelected(
                        row: Int,
                        column: Int,
                    ) {
                        selectedTiles += row to column
                    }
                },
            )
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            addChild(battlefieldView)
            // When
            clickTile(battlefieldView, row = 0, column = 0)
            // Then
            selectedTiles.shouldContainExactly(0 to 0)
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
