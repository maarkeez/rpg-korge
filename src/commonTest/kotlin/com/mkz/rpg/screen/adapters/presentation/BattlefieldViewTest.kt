package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.BattlefieldHealthBarPreviewView.PreviewOption
import com.mkz.rpg.screen.adapters.presentation.BattlefieldView.Following.TRAIL
import com.mkz.rpg.shared.adapters.presentation.awaitCompletion
import com.mkz.rpg.shared.adapters.presentation.nextFrame
import com.mkz.rpg.shared.adapters.presentation.sampleUntilCompletion
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.shared.adapters.presentation.startAnimation
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import korlibs.image.bitmap.Bitmap
import korlibs.image.format.readBitmap
import korlibs.korge.tests.ViewsForTesting
import kotlinx.coroutines.Job
import kotlin.test.Test

class BattlefieldViewTest : ViewsForTesting() {
    private companion object {
        // The battlefield scroll animation lasts 300ms and each test frame 10ms.
        const val SCROLL_ANIMATION_FRAMES = 40

        // A moving battle unit glides 150ms to each tile and each test frame lasts 10ms.
        const val MOVE_STEP_FRAMES = 15

        // Trailing, the camera catches up with a battle unit 6 tiles away within this many frames of 10ms.
        const val CATCH_UP_FRAMES = 40

        // A swap lasts 150ms and each test frame 10ms.
        const val SWAP_FRAMES = 15

        // The tiles on the screen in each direction when the battlefield is not scrolled: the 8 in the viewport and 1 more.
        const val WINDOW_TILES = BattlefieldView.VISIBLE_TILES + BattlefieldView.TILE_WINDOW_MARGIN
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
    fun `DisplayBattlefield should scroll the battlefield by the distance dragged when is dragged`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            addChild(battlefieldView)
            // When
            dragBattlefield(battlefieldView, -48.0 to -48.0, -96.0 to -96.0)
            // Then
            battlefieldView.scrollOffset() shouldBe (-96.0 to -96.0)
        }

    @Test
    fun `DisplayHealthPreview should display the health preview instead of the selection when is displayed on a tile`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayPotentialCast(row = 0, column = 0)
            // When
            battlefieldView.displayHealthPreview(row = 0, column = 0, previewOption = PreviewOption.TWO_OUT_OF_THREE)
            // Then
            battlefieldView.healthPreviewAt(row = 0, column = 0) shouldBe PreviewOption.TWO_OUT_OF_THREE
            battlefieldView.hasSelectionAt(row = 0, column = 0).shouldBeFalse()
        }

    @Test
    fun `DisplayHealthPreview should display the health preview at the top of the tile, as wide as the tile and a fifth as high`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            // When
            battlefieldView.displayHealthPreview(row = 0, column = 0, previewOption = PreviewOption.FULL)
            // Then
            val tileSize = BattlefieldView.TILE_SIZE.toDouble()
            battlefieldView.healthPreviewPlacementAt(row = 0, column = 0) shouldBe TilePlacement(x = 0.0, y = 0.0, width = tileSize, height = tileSize / 5)
        }

    @Test
    fun `DisplayHealthPreview should replace the previous health preview when is displayed again on the tile`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayHealthPreview(row = 0, column = 0, previewOption = PreviewOption.ONE_OUT_OF_THREE)
            val previousBlinks = battlefieldView.healthPreviewBlinksAt(row = 0, column = 0)
            // When
            battlefieldView.displayHealthPreview(row = 0, column = 0, previewOption = PreviewOption.FULL)
            // Then
            battlefieldView.healthPreviewAt(row = 0, column = 0) shouldBe PreviewOption.FULL
            previousBlinks.none { blink -> blink.isBlinking() }.shouldBeTrue()
        }

    @Test
    fun `ResetTiles should remove the health previews and stop their blinks when the tiles are reset`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayHealthPreview(row = 0, column = 0, previewOption = PreviewOption.ONE_OUT_OF_THREE)
            val blinks = battlefieldView.healthPreviewBlinksAt(row = 0, column = 0)
            // When
            battlefieldView.resetTiles()
            // Then
            battlefieldView.healthPreviewAt(row = 0, column = 0).shouldBeNull()
            blinks.shouldNotBeEmpty()
            blinks.none { blink -> blink.isBlinking() }.shouldBeTrue()
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
    fun `DisplayBattlefield should display the tiles without a shadow when battlefield is displayed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            val battlefield = battlefield(rows = 2, columns = 2).toDto()
            // When
            battlefieldView.displayBattlefield(battlefield)
            // Then
            battlefieldView.hasShadowAt(row = 0, column = 0).shouldBeFalse()
            battlefieldView.hasShadowAt(row = 1, column = 1).shouldBeFalse()
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
            val transitionStrip = ResourcesBundle.root["terrain/transitions/sand_to_void.png"].readBitmap()
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
            val transitionStrip = ResourcesBundle.root["terrain/transitions/sand_to_void.png"].readBitmap()
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
            val transitionStrip = ResourcesBundle.root["terrain/transitions/sand_to_void.png"].readBitmap()
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
            val transitionStrip = ResourcesBundle.root["terrain/transitions/sand_to_void.png"].readBitmap()
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
    fun `DisplayNpc should display the npc on its tile, and no battle unit, when is displayed`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            // When
            battlefieldView.displayNpc(row = 0, column = 0, npcId = "the-guy", unitId = null)
            // Then
            battlefieldView.hasNpcAt(row = 0, column = 0).shouldBeTrue()
            battlefieldView.hasBattleUnitAt(row = 0, column = 0).shouldBeFalse()
        }

    @Test
    fun `DisplayNpc should display a recruitable npc with the image of its unit`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            // When
            battlefieldView.displayNpc(row = 0, column = 0, npcId = "a-knight-to-recruit", unitId = "knight")
            // Then
            battlefieldView.hasNpcAt(row = 0, column = 0).shouldBeTrue()
        }

    @Test
    fun `RemoveNpc should remove the npc from its tile`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayNpc(row = 0, column = 0, npcId = "the-guy", unitId = null)
            // When
            battlefieldView.removeNpc(row = 0, column = 0)
            // Then
            battlefieldView.hasNpcAt(row = 0, column = 0).shouldBeFalse()
        }

    @Test
    fun `DisplayNpc should not display the npc when it has no image`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            // When
            battlefieldView.displayNpc(row = 0, column = 0, npcId = "the-ghost", unitId = null)
            // Then
            battlefieldView.hasNpcAt(row = 0, column = 0).shouldBeFalse()
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
    fun `DisplayBattlefield should replace the displayed battlefield when a battlefield is displayed again`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            // When
            battlefieldView.displayBattlefield(battlefield(rows = 4, columns = 6).toDto())
            // Then
            battlefieldView.displayedBattlefieldCount() shouldBe 1
            battlefieldView.tileCount() shouldBe 24
        }

    @Test
    fun `DisplayBattlefield should keep the scroll as far as the new size allows when a battlefield is displayed again`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            addChild(battlefieldView)
            dragBattlefield(battlefieldView, -48.0 to -300.0)
            // When
            battlefieldView.displayBattlefield(battlefield(rows = 10, columns = 16).toDto())
            // Then
            battlefieldView.scrollOffset() shouldBe (-48.0 to -96.0)
        }

    @Test
    fun `DisplayBattlefield should scroll the battlefield by the distance dragged when it is dragged after being displayed again`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            addChild(battlefieldView)
            // When
            dragBattlefield(battlefieldView, -48.0 to -96.0)
            // Then
            battlefieldView.scrollOffset() shouldBe (-48.0 to -96.0)
        }

    @Test
    fun `DisplayBattlefield should only create the tiles in the viewport and around it when the battlefield is large`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            // When
            battlefieldView.displayBattlefield(battlefield(rows = 128, columns = 128).toDto())
            // Then
            battlefieldView.tileCount() shouldBe 128 * 128
            battlefieldView.createdTileCount() shouldBe WINDOW_TILES * WINDOW_TILES
            battlefieldView.tilesOnScreenCount() shouldBe WINDOW_TILES * WINDOW_TILES
        }

    @Test
    fun `DisplayBattlefield should put the scrolled tiles on the screen and drop the far ones when a large battlefield is scrolled`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 128, columns = 128).toDto())
            addChild(battlefieldView)
            // When
            dragBattlefield(battlefieldView, -2400.0 to -2400.0)
            // Then
            battlefieldView.isTileVisible(row = 52, column = 52).shouldBeTrue()
            battlefieldView.hasTerrainAt(row = 52, column = 52).shouldBeTrue()
            battlefieldView.tilesOnScreenCount() shouldBe (WINDOW_TILES + 1) * (WINDOW_TILES + 1)
            battlefieldView.createdTileCount() shouldBe (WINDOW_TILES + 1) * (WINDOW_TILES + 1)
        }

    @Test
    fun `DisplayBattlefield should keep the battle unit of a tile when the tile is scrolled out and back`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 64, columns = 64).toDto())
            battlefieldView.displayBattleUnit(row = 1, column = 1, unitId = "knight")
            addChild(battlefieldView)
            dragBattlefield(battlefieldView, -1200.0 to -1200.0)
            require(!battlefieldView.isTileVisible(row = 1, column = 1))
            // When
            dragBattlefield(battlefieldView, 0.0 to 0.0, 1200.0 to 1200.0)
            // Then
            battlefieldView.hasBattleUnitAt(row = 1, column = 1).shouldBeTrue()
            battlefieldView.isTileOnScreen(row = 1, column = 1).shouldBeTrue()
        }

    @Test
    fun `DimTilesExcept should dim the tiles created later when they are scrolled into the viewport`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 64, columns = 64).toDto())
            addChild(battlefieldView)
            battlefieldView.dimTilesExcept(setOf(Battlefield.Dto.PositionDto(30, 31)))
            // When
            dragBattlefield(battlefieldView, -1300.0 to -1300.0)
            // Then
            battlefieldView.isDimmedAt(row = 30, column = 30).shouldBeTrue()
            battlefieldView.isDimmedAt(row = 30, column = 31).shouldBeFalse()
        }

    @Test
    fun `UpdateTerrain should create the tiles scrolled into the viewport with the updated terrain when they were not created`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 64, columns = 64).toDto())
            addChild(battlefieldView)
            val paintedTiles = List(64) { row -> List(64) { column -> if (row == 30 && column == 30) "void" else "sand" } }
            val paintedBattlefield = battlefield(64, 64, paintedTiles, setOf(BattlefieldMother.terrainTransitionRule("sand", "void"))).toDto()
            battlefieldView.updateTerrain(paintedBattlefield, listOf(Battlefield.Dto.PositionDto(30, 30)))
            // When
            dragBattlefield(battlefieldView, -1300.0 to -1300.0)
            // Then
            battlefieldView.terrainBitmapAt(row = 30, column = 30) shouldBe battlefieldView.terrainTileBitmap("void", terrainTransition = null)
        }

    @Test
    fun `PickUpLoot should finish lifting the item when its tile is scrolled out of the viewport meanwhile`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 64, columns = 64).toDto())
            addChild(battlefieldView)
            val pickingUp = startAnimation { battlefieldView.pickUpLoot(row = 0, column = 0, lootableItemId = "health-potion", remainingLootableItemIds = emptyList()) }
            // When
            dragBattlefield(battlefieldView, -1200.0 to -1200.0)
            awaitCompletion(pickingUp)
            // Then
            pickingUp.isCompleted.shouldBeTrue()
            battlefieldView.liftedLootAt(row = 0, column = 0).shouldBeNull()
        }

    @Test
    fun `UpdateTerrain should draw again the terrain and the transitions of the tiles when the terrain is updated`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(map = ". . .").toDto())
            // As in the game, only sand has a transition to void.
            val paintedTiles = listOf(listOf("sand", "void", "sand"))
            val paintedBattlefield = battlefield(1, 3, paintedTiles, setOf(BattlefieldMother.terrainTransitionRule("sand", "void"))).toDto()
            // When
            battlefieldView.updateTerrain(paintedBattlefield, listOf(Battlefield.Dto.PositionDto(0, 0), Battlefield.Dto.PositionDto(0, 1)))
            // Then
            val tileBitmap = { column: Int ->
                val tile = paintedBattlefield.tiles.getValue(Battlefield.Dto.PositionDto(0, column))
                battlefieldView.terrainTileBitmap(tile.terrainId, tile.terrainTransition)
            }
            battlefieldView.terrainBitmapAt(row = 0, column = 0) shouldBe tileBitmap(0)
            battlefieldView.terrainBitmapAt(row = 0, column = 1) shouldBe tileBitmap(1)
            battlefieldView.terrainBitmapAt(row = 0, column = 2) shouldBe battlefieldView.terrainTileBitmap("sand", terrainTransition = null)
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

    @Test
    fun `PickUpLoot should lift the item icon over the battle unit, growing and fading out, when the item is picked up`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val battlefieldView = BattlefieldView(resourcesLoader)
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayLoot(row = 0, column = 0, lootableItemIds = listOf("health-potion"))
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            // When
            val pickingUp = startPickingUp(battlefieldView, lootableItemId = "health-potion", remainingLootableItemIds = emptyList())
            val isOverBattleUnit = battlefieldView.isLiftedLootOverBattleUnitAt(row = 0, column = 0)
            val liftedLoot = sampleUntilCompletion(pickingUp) { battlefieldView.liftedLootAt(row = 0, column = 0) }.filterNotNull()
            // Then
            isOverBattleUnit.shouldBeTrue()
            liftedLoot.map(LiftedLoot::bitmap).distinct().shouldContainExactly(resourcesLoader.bitmap("lootableItem/health-potion.png"))
            liftedLoot.first().rise shouldBe (0.0 plusOrMinus 0.01)
            liftedLoot.last().rise shouldBe (BattlefieldView.PICK_UP_RISE plusOrMinus 3.0)
            liftedLoot.first().scale shouldBe (BattlefieldView.LOOT_SCALE plusOrMinus 0.01)
            liftedLoot.last().scale shouldBe (BattlefieldView.PICK_UP_SCALE plusOrMinus 0.1)
            liftedLoot.first().alpha shouldBe (1.0 plusOrMinus 0.01)
            liftedLoot.last().alpha shouldBe (0.0 plusOrMinus 0.1)
            liftedLoot.zipWithNext().all { (previous, next) -> next.rise >= previous.rise && next.alpha <= previous.alpha }.shouldBeTrue()
            battlefieldView.liftedLootAt(row = 0, column = 0).shouldBeNull()
            battlefieldView.hasBattleUnitAt(row = 0, column = 0).shouldBeTrue()
        }

    @Test
    fun `PickUpLoot should display the items left on the tile when the item is picked up`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val battlefieldView = BattlefieldView(resourcesLoader)
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayLoot(row = 0, column = 0, lootableItemIds = listOf("health-potion", "hammer"))
            addChild(battlefieldView)
            // When
            val pickingUp = startPickingUp(battlefieldView, lootableItemId = "health-potion", remainingLootableItemIds = listOf("hammer"))
            // Then
            battlefieldView.lootBitmapAt(row = 0, column = 0) shouldBe resourcesLoader.bitmap("lootableItem/hammer.png")
            battlefieldView.liftedLootAt(row = 0, column = 0)?.bitmap shouldBe resourcesLoader.bitmap("lootableItem/health-potion.png")
            awaitCompletion(pickingUp)
        }

    @Test
    fun `PickUpLoot should scroll the battlefield to the tile when the item is picked up out of the viewport`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayLoot(row = 12, column = 13, lootableItemIds = listOf("health-potion"))
            addChild(battlefieldView)
            require(!battlefieldView.isTileVisible(row = 12, column = 13))
            // When
            awaitCompletion(startPickingUp(battlefieldView, row = 12, column = 13, lootableItemId = "health-potion", remainingLootableItemIds = emptyList()))
            // Then
            battlefieldView.isTileVisible(row = 12, column = 13).shouldBeTrue()
            battlefieldView.lootBitmapAt(row = 12, column = 13).shouldBeNull()
        }

    @Test
    fun `PickUpLoot should not lift anything when the item has no image`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayLoot(row = 0, column = 0, lootableItemIds = listOf("health-potion", "crown"))
            addChild(battlefieldView)
            // When
            val pickingUp = startPickingUp(battlefieldView, lootableItemId = "crown", remainingLootableItemIds = listOf("health-potion"))
            // Then
            pickingUp.isCompleted.shouldBeTrue()
            battlefieldView.liftedLootAt(row = 0, column = 0).shouldBeNull()
            battlefieldView.lootBitmapAt(row = 0, column = 0) shouldBe loadedResourcesLoader().bitmap("lootableItem/health-potion.png")
        }

    private suspend fun startPickingUp(
        battlefieldView: BattlefieldView,
        row: Int = 0,
        column: Int = 0,
        lootableItemId: String,
        remainingLootableItemIds: List<String>,
    ): Job =
        startAnimation {
            battlefieldView.pickUpLoot(row = row, column = column, lootableItemId = lootableItemId, remainingLootableItemIds = remainingLootableItemIds)
        }

    private val path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1), Battlefield.Dto.PositionDto(row = 0, column = 2))

    @Test
    fun `MoveBattleUnit should glide the battle unit image along its path, off the tiles, when it moves`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 3).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            // When
            val moving = startMoving(battlefieldView, path)
            val frames = sampleUntilCompletion(moving) { battlefieldView.battleUnitPositions() to battlefieldView.glidingBattleUnits().map { glidingUnit -> glidingUnit.x } }
            // Then
            frames.shouldNotBeEmpty()
            frames.forEach { (positions, _) -> positions.shouldBeEmpty() }
            val glidingXs = frames.map { (_, glidingXs) -> glidingXs.single() }
            glidingXs.zipWithNext().forEach { (previous, next) -> next shouldBeGreaterThanOrEqual previous }
            glidingXs.any { glidingX -> glidingX > 0.0 && glidingX < BattlefieldView.TILE_SIZE }.shouldBeTrue()
        }

    @Test
    fun `MoveBattleUnit should display the battle unit on its destination tile once it has glided along its path`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 3).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            // When
            awaitCompletion(startMoving(battlefieldView, path))
            // Then
            battlefieldView.battleUnitPositions().shouldContainExactly(0 to 2)
            battlefieldView.glidingBattleUnits().shouldBeEmpty()
        }

    @Test
    fun `MoveBattleUnit should glide each tile of the path for a step when the battle unit moves`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 3).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            // When
            val frames = sampleUntilCompletion(startMoving(battlefieldView, path)) { }
            // Then
            frames.size shouldBeInRange (2 * MOVE_STEP_FRAMES - 2)..(2 * MOVE_STEP_FRAMES + 2)
        }

    @Test
    fun `MoveBattleUnit should jump to the destination when the battle unit teleports`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 3).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            // When
            val moving = startMoving(battlefieldView, listOf(Battlefield.Dto.PositionDto(row = 0, column = 2)))
            val glidingXs = sampleUntilCompletion(moving) { battlefieldView.glidingBattleUnits().map { glidingUnit -> glidingUnit.x } }
            // Then
            glidingXs.shouldNotBeEmpty()
            glidingXs.forEach { xs -> xs.shouldContainExactly(2.0 * BattlefieldView.TILE_SIZE) }
            battlefieldView.battleUnitPositions().shouldContainExactly(0 to 2)
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

    @Test
    fun `MoveBattleUnit should not scroll the battlefield when the battle unit moves along a border of the battlefield`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 3, column = 0, unitId = "knight")
            addChild(battlefieldView)
            // When
            awaitCompletion(startMoving(battlefieldView, listOf(Battlefield.Dto.PositionDto(row = 4, column = 0))))
            // Then
            battlefieldView.scrollOffset() shouldBe (0.0 to 0.0)
        }

    @Test
    fun `MoveBattleUnit should only scroll the axis whose viewport edge the battle unit reaches when it moves`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 5, column = 6, unitId = "knight")
            addChild(battlefieldView)
            // When
            awaitCompletion(startMoving(battlefieldView, listOf(Battlefield.Dto.PositionDto(row = 5, column = 7))))
            // Then
            battlefieldView.scrollOffset() shouldBe (-168.0 to 0.0)
        }

    @Test
    fun `MoveBattleUnit should not scroll the battlefield when the battle unit moves inside the deadzone trailing`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 3, column = 3, unitId = "knight")
            addChild(battlefieldView)
            // When
            awaitCompletion(startMoving(battlefieldView, listOf(Battlefield.Dto.PositionDto(row = 3, column = 4)), TRAIL))
            nextFrame()
            // Then
            battlefieldView.scrollOffset() shouldBe (0.0 to 0.0)
        }

    @Test
    fun `MoveBattleUnit should scroll the battlefield with the battle unit when it walks past the deadzone trailing`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 3, column = 4, unitId = "knight")
            addChild(battlefieldView)
            // When
            val moving = startMoving(battlefieldView, listOf(Battlefield.Dto.PositionDto(row = 3, column = 5)), TRAIL)
            val frames = sampleUntilCompletion(moving) { battlefieldView.glidingBattleUnits().single().x to battlefieldView.scrollOffset().first }
            nextFrame()
            // Then
            frames.forEach { (glidingX, scrollX) -> glidingX + scrollX shouldBe (BattlefieldView.DEADZONE_END.toDouble() plusOrMinus 0.001) }
            frames.any { (_, scrollX) -> scrollX > -BattlefieldView.TILE_SIZE && scrollX < 0.0 }.shouldBeTrue()
            battlefieldView.scrollOffset() shouldBe (-48.0 to 0.0)
        }

    @Test
    fun `MoveBattleUnit should not wait for the battlefield to scroll trailing`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 3, column = 7, unitId = "knight")
            addChild(battlefieldView)
            // When
            val frames = sampleUntilCompletion(startMoving(battlefieldView, listOf(Battlefield.Dto.PositionDto(row = 3, column = 8)), TRAIL)) { }
            // Then
            frames.size shouldBeInRange (MOVE_STEP_FRAMES - 1)..(MOVE_STEP_FRAMES + 1)
        }

    @Test
    fun `MoveBattleUnit should not scroll back until the battle unit crosses the deadzone when it turns around trailing`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 3, column = 4, unitId = "knight")
            addChild(battlefieldView)
            awaitCompletion(startMoving(battlefieldView, listOf(Battlefield.Dto.PositionDto(row = 3, column = 5)), TRAIL))
            nextFrame()
            require(battlefieldView.scrollOffset() == (-48.0 to 0.0))
            // When
            awaitCompletion(startMoving(battlefieldView, listOf(Battlefield.Dto.PositionDto(row = 3, column = 4)), TRAIL))
            nextFrame()
            // Then
            battlefieldView.scrollOffset() shouldBe (-48.0 to 0.0)
        }

    @Test
    fun `MoveBattleUnit should clamp the trailing scroll to the battlefield when the battle unit walks near its border`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 9).toDto())
            battlefieldView.displayBattleUnit(row = 3, column = 4, unitId = "knight")
            addChild(battlefieldView)
            // When
            val path = listOf(Battlefield.Dto.PositionDto(row = 3, column = 5), Battlefield.Dto.PositionDto(row = 3, column = 6))
            awaitCompletion(startMoving(battlefieldView, path, TRAIL))
            nextFrame()
            // Then
            battlefieldView.scrollOffset() shouldBe (-48.0 to 0.0)
        }

    @Test
    fun `Trail should ease the battlefield to the battle unit when it is far from the deadzone`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 10, column = 10, unitId = "knight")
            addChild(battlefieldView)
            // When
            battlefieldView.trail(row = 10, column = 10)
            val scrolls = List(CATCH_UP_FRAMES) { battlefieldView.scrollOffset().also { nextFrame() } } + listOf(battlefieldView.scrollOffset())
            // Then
            scrolls.last() shouldBe (-288.0 to -288.0)
            val easingScrolls = scrolls.takeWhile { scroll -> scroll != scrolls.last() }
            easingScrolls.size shouldBeGreaterThan 2
            easingScrolls.zipWithNext().forEach { (previous, next) -> next.first shouldBeLessThan previous.first }
        }

    @Test
    fun `StopTrailing should leave the scroll where it is`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 10, column = 10, unitId = "knight")
            addChild(battlefieldView)
            battlefieldView.trail(row = 10, column = 10)
            // When
            battlefieldView.stopTrailing()
            repeat(CATCH_UP_FRAMES) { nextFrame() }
            // Then
            battlefieldView.scrollOffset() shouldBe (0.0 to 0.0)
        }

    @Test
    fun `DisplayBattlefield should pause trailing when the battlefield is dragged`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 3, column = 3, unitId = "knight")
            addChild(battlefieldView)
            battlefieldView.trail(row = 3, column = 3)
            // When
            dragBattlefield(battlefieldView, -48.0 to -48.0, -96.0 to -96.0)
            repeat(CATCH_UP_FRAMES) { nextFrame() }
            // Then
            battlefieldView.scrollOffset() shouldBe (-96.0 to -96.0)
        }

    @Test
    fun `MoveBattleUnit should resume trailing when the battle unit moves after a drag trailing`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 3, column = 3, unitId = "knight")
            addChild(battlefieldView)
            battlefieldView.trail(row = 3, column = 3)
            dragBattlefield(battlefieldView, -96.0 to -96.0)
            // When
            awaitCompletion(startMoving(battlefieldView, listOf(Battlefield.Dto.PositionDto(row = 3, column = 4)), TRAIL))
            repeat(CATCH_UP_FRAMES) { nextFrame() }
            // Then: the camera caught up while the battle unit glided, until the battle unit was inside the deadzone.
            val (scrollX, scrollY) = battlefieldView.scrollOffset()
            val deadzone = BattlefieldView.DEADZONE_START.toDouble()..BattlefieldView.DEADZONE_END.toDouble()
            (4 * BattlefieldView.TILE_SIZE + scrollX in deadzone).shouldBeTrue()
            (3 * BattlefieldView.TILE_SIZE + scrollY in deadzone).shouldBeTrue()
        }

    @Test
    fun `SwapBattleUnits should display each battle unit on the other's tile when the swap finishes`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val battlefieldView = swappingBattlefieldView(resourcesLoader)
            addChild(battlefieldView)
            // When
            awaitCompletion(startSwapping(battlefieldView))
            // Then
            battlefieldView.battleUnitBitmapAt(row = 0, column = 0) shouldBe resourcesLoader.bitmap("unit/rat.png")
            battlefieldView.battleUnitBitmapAt(row = 0, column = 1) shouldBe resourcesLoader.bitmap("unit/knight.png")
            battlefieldView.glidingBattleUnits().shouldBeEmpty()
        }

    @Test
    fun `SwapBattleUnits should not display the battle units on their tiles while they swap`() =
        sharedViewsTest {
            // Given
            val battlefieldView = swappingBattlefieldView(loadedResourcesLoader())
            addChild(battlefieldView)
            // When
            val positions = sampleUntilCompletion(startSwapping(battlefieldView)) { battlefieldView.battleUnitPositions() }
            // Then
            positions.shouldNotBeEmpty()
            positions.forEach { swappingPositions -> swappingPositions.shouldBeEmpty() }
        }

    @Test
    fun `SwapBattleUnits should hop the leader over the member while they swap`() =
        sharedViewsTest {
            // Given
            val battlefieldView = swappingBattlefieldView(loadedResourcesLoader())
            addChild(battlefieldView)
            // When
            val frames = sampleUntilCompletion(startSwapping(battlefieldView)) { battlefieldView.glidingBattleUnits() }
            // Then
            frames.shouldNotBeEmpty()
            frames.forEach { glidingUnits ->
                glidingUnits.map { glidingUnit -> glidingUnit.name }.shouldContainExactly(BattlefieldView.SWAPPING_MEMBER, BattlefieldView.SWAPPING_LEADER)
                glidingUnits.first().y shouldBe 0.0
            }
            val highestLeaderY = frames.minOf { glidingUnits -> glidingUnits.last().y }
            highestLeaderY shouldBe (-BattlefieldView.SWAP_HOP_HEIGHT plusOrMinus 1.0)
        }

    @Test
    fun `SwapBattleUnits should last the swap duration`() =
        sharedViewsTest {
            // Given
            val battlefieldView = swappingBattlefieldView(loadedResourcesLoader())
            addChild(battlefieldView)
            // When
            val frames = sampleUntilCompletion(startSwapping(battlefieldView)) { }
            // Then
            frames.size shouldBeInRange (SWAP_FRAMES - 1)..(SWAP_FRAMES + 1)
        }

    @Test
    fun `SwapBattleUnits should trail the leader while it hops over the member`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 3, column = 4, unitId = "knight")
            battlefieldView.displayBattleUnit(row = 3, column = 5, unitId = "rat")
            addChild(battlefieldView)
            // When
            val swapping =
                startAnimation {
                    battlefieldView.swapBattleUnits(leaderRow = 3, leaderColumn = 4, leaderUnitId = "knight", memberRow = 3, memberColumn = 5, memberUnitId = "rat")
                }
            val frames = sampleUntilCompletion(swapping) { battlefieldView.glidingBattleUnits().last().x to battlefieldView.scrollOffset().first }
            nextFrame()
            // Then
            frames.forEach { (leaderX, scrollX) -> leaderX + scrollX shouldBe (BattlefieldView.DEADZONE_END.toDouble() plusOrMinus 0.001) }
            battlefieldView.scrollOffset() shouldBe (-48.0 to 0.0)
        }

    // The leader, a knight, stands at (0,0), and the member, a rat, on its east at (0,1).
    private fun swappingBattlefieldView(resourcesLoader: BattleSceneResourcesLoader): BattlefieldView {
        val battlefieldView = BattlefieldView(resourcesLoader)
        battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 2).toDto())
        battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
        battlefieldView.displayBattleUnit(row = 0, column = 1, unitId = "rat")
        return battlefieldView
    }

    private suspend fun startSwapping(battlefieldView: BattlefieldView): Job =
        startAnimation {
            battlefieldView.swapBattleUnits(leaderRow = 0, leaderColumn = 0, leaderUnitId = "knight", memberRow = 0, memberColumn = 1, memberUnitId = "rat")
        }

    private suspend fun startMoving(
        battlefieldView: BattlefieldView,
        path: List<Battlefield.Dto.PositionDto>,
        following: BattlefieldView.Following = BattlefieldView.Following.CENTER,
    ): Job {
        val origin = battlefieldView.battleUnitPositions().single()
        return startAnimation {
            battlefieldView.moveBattleUnit(fromRow = origin.first, fromColumn = origin.second, path = path, unitId = "knight", following = following)
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
    fun `FlashBattleUnit should stop blinking when the battle unit leaves its tile`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 2).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            val flashing = startFlashing(battlefieldView, row = 0, column = 0, unitId = "knight")
            nextFrame()
            require(battlefieldView.isFlashingAt(row = 0, column = 0))
            // When
            val moving = startMoving(battlefieldView, path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1)))
            val flashingFrames = sampleUntilCompletion(moving) { battlefieldView.isFlashingAt(row = 0, column = 0) }
            // Then
            withClue("The tile it left never blinks") { flashingFrames.shouldNotContain(true) }
            flashing.isCompleted.shouldBeTrue()
            battlefieldView.isFlashingAt(row = 0, column = 1).shouldBeFalse()
        }

    @Test
    fun `FlashBattleUnit should blink after the flash already on the tile when the battle unit is flashed twice at once`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 1, columns = 1).toDto())
            battlefieldView.displayBattleUnit(row = 0, column = 0, unitId = "knight")
            addChild(battlefieldView)
            // When
            val firstFlashing = startFlashing(battlefieldView, row = 0, column = 0, unitId = "knight")
            val secondFlashing = startFlashing(battlefieldView, row = 0, column = 0, unitId = "knight")
            val flashingFrames = sampleUntilCompletion(secondFlashing) { battlefieldView.isFlashingAt(row = 0, column = 0) }
            // Then
            firstFlashing.isCompleted.shouldBeTrue()
            val blinks = flashingFrames.zipWithNext().count { (previous, next) -> !previous && next } + if (flashingFrames.first()) 1 else 0
            blinks shouldBe 2 * BattlefieldView.FLASH_COUNT
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
    fun `FlashBattleUnit should not scroll the battlefield trailing`() =
        sharedViewsTest {
            // Given
            val battlefieldView = BattlefieldView(loadedResourcesLoader())
            battlefieldView.displayBattlefield(battlefield(rows = 16, columns = 16).toDto())
            battlefieldView.displayBattleUnit(row = 3, column = 3, unitId = "knight")
            battlefieldView.displayBattleUnit(row = 12, column = 13, unitId = "rat")
            addChild(battlefieldView)
            battlefieldView.trail(row = 3, column = 3)
            // When
            awaitCompletion(startFlashing(battlefieldView, row = 12, column = 13, unitId = "rat"))
            // Then
            battlefieldView.scrollOffset() shouldBe (0.0 to 0.0)
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
