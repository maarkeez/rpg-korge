package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattlefieldHealthBarPreviewView.PreviewOption
import com.mkz.rpg.screen.adapters.presentation.BattlefieldTerrain
import com.mkz.rpg.screen.adapters.presentation.BattlefieldView
import com.mkz.rpg.screen.adapters.presentation.TerrainTransition
import com.mkz.rpg.screen.adapters.presentation.TilePosition
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.io.async.launchImmediately
import korlibs.korge.view.View
import korlibs.korge.view.addUpdater
import kotlinx.coroutines.CoroutineScope

/**
 * The stories of [BattlefieldView]: a tiny fixed map, as big as its viewport, with a few tiles of each terrain, a battle
 * unit and another, an npc, a building and loot. Tapping a tile logs `tileSelected` and selects it. The health flash plays
 * once the battlefield is on the screen, and again whenever an arg changes, e.g.: `replay`.
 */
object BattlefieldViewStories : StoryBinding {
    // Each tile is a terrain: sand, grass, water, lava or void. The fortress stands over the sand on its right.
    private val MAP =
        listOf(
            "GGSSSS",
            "GSSSSS",
            "SSSSSS",
            "WSSSSS",
            "WSSSSL",
            "WWSSVL",
        )
    private val TERRAINS = mapOf('S' to "sand", 'G' to "grass", 'W' to "water", 'L' to "lava", 'V' to "void")
    private const val TRANSITION_NAME_SEPARATOR = "_to_"

    // The battlefield domain gives each transition the sides of the tile that touch the target terrain these weights.
    private const val NORTH_WEIGHT = 1
    private const val EAST_WEIGHT = 2
    private const val SOUTH_WEIGHT = 4
    private const val WEST_WEIGHT = 8

    private val BUILDING_TILE = TilePosition(row = 0, column = 2)
    private val NPC_TILE = TilePosition(row = 2, column = 0)
    private val BATTLE_UNIT_TILE = TilePosition(row = 4, column = 2)
    private val OTHER_BATTLE_UNIT_TILE = TilePosition(row = 4, column = 4)
    private val LOOT_TILE = TilePosition(row = 3, column = 1)
    private val CHEST_TILE = TilePosition(row = 5, column = 3)

    // The tiles the battle unit can move to, and cast on, and the tiles of the cast previewed: the battlefield domain
    // works them out in the game, which the Storybook can not import.
    private val MOVE_RANGE = listOf(TilePosition(4, 1), TilePosition(4, 3), TilePosition(5, 2), TilePosition(3, 1), TilePosition(5, 3))
    private val CAST_RANGE = listOf(TilePosition(4, 1), TilePosition(4, 3), TilePosition(4, 4), TilePosition(5, 2), TilePosition(5, 3), TilePosition(3, 1))
    private val CAST_GROUP = listOf(TilePosition(4, 3), OTHER_BATTLE_UNIT_TILE, TilePosition(5, 3))

    private const val DEFAULT_UNIT = "knight"
    private const val OTHER_UNIT = "rat"
    private const val DEFAULT_NPC = "alchemist"
    private const val DEFAULT_LOOT = "health-potion"
    private const val BUILDING = "fortress"
    private const val CHEST_ITEMS = 2

    // The option of the selected tile and of the picks when none is meant, which no tile nor npc is.
    private const val NONE = "none"

    private const val LAYER = "layer"
    private const val SELECTED_TILE = "selectedTile"
    private const val HEALTH_PREVIEW = "healthPreview"
    private const val UNIT = "unit"
    private const val NPC = "npc"
    private const val LOOT = "loot"
    private const val REPLAY = "replay"
    private const val REPLAY_MAXIMUM = 9

    /** What is drawn over the tiles, as the battle does for the battle unit selected. */
    private enum class Layer {
        NONE,
        MOVE_RANGE,
        CAST_RANGE,
        HEALTH_PREVIEW,
        HEALTH_FLASH,
    }

    private val map =
        StoryDto(id = "screen-battlefield--map", name = "Map", description = "The tiny map as the battlefield draws it, with nothing selected.", args = emptyMap())
    private val moveRange =
        StoryDto(
            id = "screen-battlefield--move-range",
            name = "Move range",
            description = "The battle unit selected: its tile selected, the tiles it can move to framed, and the others dimmed.",
            args = mapOf(LAYER to ArgDto.text(Layer.MOVE_RANGE.name), SELECTED_TILE to ArgDto.text("${BATTLE_UNIT_TILE.row},${BATTLE_UNIT_TILE.column}")),
        )
    private val castRange =
        StoryDto(
            id = "screen-battlefield--cast-range",
            name = "Cast range",
            description = "An ability selected: the tiles it can be cast on framed, and the others dimmed.",
            args = mapOf(LAYER to ArgDto.text(Layer.CAST_RANGE.name)),
        )
    private val selectedTile =
        StoryDto(
            id = "screen-battlefield--selected-tile",
            name = "Selected tile",
            description = "A tile selected, e.g.: by tapping it: the tile is framed and logged in the Actions as `tileSelected`.",
            args = mapOf(SELECTED_TILE to ArgDto.text("${LOOT_TILE.row},${LOOT_TILE.column}")),
        )
    private val healthPreview =
        StoryDto(
            id = "screen-battlefield--health-preview",
            name = "Health preview",
            description = "A cast previewed: the battle unit on the cast tiles shows the health it would have left, over its tile, and the vacant tiles are selected.",
            args = mapOf(LAYER to ArgDto.text(Layer.HEALTH_PREVIEW.name), HEALTH_PREVIEW to ArgDto.text(PreviewOption.TWO_OUT_OF_THREE.name)),
        )
    private val defeatedPreview =
        StoryDto(
            id = "screen-battlefield--health-preview-defeated",
            name = "Health preview, defeated",
            description = "A cast that would defeat the battle unit: its health preview is empty.",
            args = mapOf(LAYER to ArgDto.text(Layer.HEALTH_PREVIEW.name), HEALTH_PREVIEW to ArgDto.text(PreviewOption.EMPTY.name)),
        )

    private val healthFlash =
        StoryDto(
            id = "screen-battlefield--health-flash",
            name = "Health flash",
            description =
                "The health of the battle unit changed while exploring, e.g.: standing on lava: it flashes, and the health it has left " +
                    "blinks over it as long as it flashes, stays still, and fades out. Change replay to play it again.",
            args = mapOf(LAYER to ArgDto.text(Layer.HEALTH_FLASH.name), HEALTH_PREVIEW to ArgDto.text(PreviewOption.TWO_OUT_OF_THREE.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-battlefield",
            group = "Screen",
            title = "Battlefield",
            description =
                "The map of the game, dragged to scroll, with its battle units, npcs, buildings and loot. " +
                    "layer: what is drawn over the tiles. selectedTile: `row,column`, or none. healthPreview: what a cast leaves, " +
                    "or the health left flashed. replay: changed to play the health flash again.",
            controls =
                listOf(
                    ControlDto.enum(LAYER, Layer.entries.map { layer -> layer.name }),
                    ControlDto.text(SELECTED_TILE),
                    ControlDto.pick(HEALTH_PREVIEW, PreviewOption.entries.map { option -> option.name }),
                    ControlDto.pick(UNIT, context.unitIds()),
                    ControlDto.pick(NPC, listOf(NONE) + context.npcIds()),
                    ControlDto.pick(LOOT, context.lootableItemIds()),
                    ControlDto.int(REPLAY, minimum = 0, maximum = REPLAY_MAXIMUM),
                ),
            args =
                mapOf(
                    LAYER to ArgDto.text(Layer.NONE.name),
                    SELECTED_TILE to ArgDto.text(NONE),
                    HEALTH_PREVIEW to ArgDto.text(PreviewOption.FULL.name),
                    UNIT to ArgDto.text(DEFAULT_UNIT),
                    NPC to ArgDto.text(DEFAULT_NPC),
                    LOOT to ArgDto.text(DEFAULT_LOOT),
                    REPLAY to ArgDto.int(0),
                ),
            stories = listOf(map, moveRange, castRange, selectedTile, healthPreview, defeatedPreview, healthFlash),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        BattlefieldView(context.resourcesLoader).apply {
            setDelegate(
                object : BattlefieldView.Delegate {
                    override fun tileSelected(
                        row: Int,
                        column: Int,
                    ) = context.action("tileSelected", row, column, setArg = SELECTED_TILE to ArgDto.text("$row,$column"))
                },
            )
            displayBattlefield(terrain(context))
            displayBuilding(BUILDING_TILE.row, BUILDING_TILE.column, BUILDING)
            args.text(NPC).takeUnless { npcId -> npcId == NONE }?.let { npcId -> displayNpc(NPC_TILE.row, NPC_TILE.column, npcId, unitId = null) }
            displayBattleUnit(BATTLE_UNIT_TILE.row, BATTLE_UNIT_TILE.column, args.text(UNIT))
            displayBattleUnit(OTHER_BATTLE_UNIT_TILE.row, OTHER_BATTLE_UNIT_TILE.column, OTHER_UNIT)
            displayLoot(LOOT_TILE.row, LOOT_TILE.column, listOf(args.text(LOOT)))
            displayLoot(CHEST_TILE.row, CHEST_TILE.column, List(CHEST_ITEMS) { args.text(LOOT) })
            val selected = selectedTile(args.text(SELECTED_TILE))
            when (Layer.valueOf(args.text(LAYER))) {
                Layer.NONE -> Unit
                Layer.MOVE_RANGE -> {
                    MOVE_RANGE.forEach { tile -> displayPotentialMovement(tile.row, tile.column) }
                    dimTilesExcept((MOVE_RANGE + selected + BATTLE_UNIT_TILE).filterNotNull().toSet())
                }
                Layer.CAST_RANGE -> {
                    CAST_RANGE.forEach { tile -> displayPotentialCast(tile.row, tile.column) }
                    dimTilesExcept((CAST_RANGE + BATTLE_UNIT_TILE).toSet())
                }
                Layer.HEALTH_PREVIEW -> {
                    CAST_GROUP.forEach { tile ->
                        if (tile == OTHER_BATTLE_UNIT_TILE) {
                            displayHealthPreview(tile.row, tile.column, PreviewOption.valueOf(args.text(HEALTH_PREVIEW)))
                        } else {
                            displayTileSelection(tile.row, tile.column)
                        }
                    }
                    dimTilesExcept((CAST_GROUP + BATTLE_UNIT_TILE).toSet())
                }
                Layer.HEALTH_FLASH -> flashHealthOnScreen(args.text(UNIT), PreviewOption.valueOf(args.text(HEALTH_PREVIEW)))
            }
            selected?.let { tile -> displayTileSelection(tile.row, tile.column) }
        }

    // Flashes the battle unit with the health it has left, as exploring does, once the battlefield is on the screen, as
    // its animations only play there.
    private fun BattlefieldView.flashHealthOnScreen(
        unitId: String,
        previewOption: PreviewOption,
    ) {
        var isFlashed = false
        addUpdater {
            val views = stage?.views ?: return@addUpdater
            if (isFlashed) return@addUpdater
            isFlashed = true
            val scope = CoroutineScope(views.coroutineContext)
            scope.launchImmediately { flashBattleUnit(BATTLE_UNIT_TILE.row, BATTLE_UNIT_TILE.column, unitId) }
            scope.launchImmediately { flashHealth(BATTLE_UNIT_TILE.row, BATTLE_UNIT_TILE.column, previewOption) }
        }
    }

    // The tile of `row,column`, or none when there is none, or it is not on the map.
    private fun selectedTile(selectedTile: String): TilePosition? {
        val (row, column) = selectedTile.split(",").map { value -> value.trim().toIntOrNull() }.takeIf { values -> values.size == 2 } ?: return null
        if (row == null || column == null || row !in MAP.indices || column !in MAP.first().indices) return null
        return TilePosition(row, column)
    }

    // The terrain of the map, with the transitions the battlefield domain would give its tiles: a tile next to the target
    // terrain of one of the strips of transitions of its terrain draws the strip tile of the sides touching it.
    private fun terrain(context: StoryContext): BattlefieldTerrain {
        val terrainIds = MAP.map { row -> row.map { letter -> TERRAINS.getValue(letter) } }
        val strips = context.terrainTransitionIds().map { transitionId -> transitionId.split(TRANSITION_NAME_SEPARATOR).let { (from, to) -> from to to } }
        val tiles =
            terrainIds
                .flatMapIndexed { row, rowTerrainIds ->
                    rowTerrainIds.mapIndexed { column, terrainId ->
                        fun neighbour(
                            rowOffset: Int,
                            columnOffset: Int,
                        ) = terrainIds.getOrNull(row + rowOffset)?.getOrNull(column + columnOffset)

                        val neighbours = listOfNotNull(neighbour(-1, 0), neighbour(0, 1), neighbour(1, 0), neighbour(0, -1)).filter { neighbourId -> neighbourId != terrainId }
                        val targetTerrainId = strips.firstOrNull { (from, to) -> from == terrainId && to in neighbours }?.second
                        val transition =
                            targetTerrainId?.let {
                                val wangIndex =
                                    (if (neighbour(-1, 0) == targetTerrainId) NORTH_WEIGHT else 0) +
                                        (if (neighbour(0, 1) == targetTerrainId) EAST_WEIGHT else 0) +
                                        (if (neighbour(1, 0) == targetTerrainId) SOUTH_WEIGHT else 0) +
                                        (if (neighbour(0, -1) == targetTerrainId) WEST_WEIGHT else 0)
                                TerrainTransition(fromTerrainId = terrainId, toTerrainId = targetTerrainId, wangIndex = wangIndex)
                            }
                        TilePosition(row, column) to BattlefieldTerrain.Tile(terrainId, transition)
                    }
                }.toMap()
        return BattlefieldTerrain(rows = MAP.size, columns = MAP.first().length, tiles = tiles)
    }
}
