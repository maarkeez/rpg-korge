package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.worldBuilder.adapters.presentation.WorldBuilderApi
import com.mkz.rpg.worldBuilder.domain.WorldBuilder
import com.mkz.rpg.worldBuilder.domain.WorldBuilderEvent
import com.mkz.rpg.worldBuilder.domain.WorldBuilderEvent.WorldImportRejected.Reason
import korlibs.io.async.launchImmediately
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope

/**
 * Displays the world being built on the battlefield, as the battle would display it, and lets the player resize it,
 * paint its tiles, export it to the clipboard, import one from a file, and leave once they confirm it.
 */
class WorldBuilderPresenter(
    private val battlefieldView: BattlefieldView,
    private val infoView: WorldBuilderInfoView,
    private val hudView: WorldBuilderHudView,
    private val callToActionView: WorldBuilderCallToActionView,
    private val menuView: WorldBuilderMenuView,
    private val worldBuilderApi: WorldBuilderApi,
    private val battlefieldApi: BattlefieldApi,
    private val terrainApi: TerrainApi,
    private val texts: Texts,
    private val clipboard: Clipboard,
    private val worldFilePicker: WorldFilePicker,
    private val coroutineScope: CoroutineScope,
    private val onLeft: () -> Unit,
    eventBus: EventBus,
) : BattlefieldView.Delegate,
    WorldBuilderHudView.Delegate {
    private val subscriptions =
        listOf(
            eventBus.subscribe<WorldBuilderEvent.WorldCreated> {
                displayWorld()
                displayTerrains()
            },
            eventBus.subscribe<WorldBuilderEvent.WorldResized> { displayWorld() },
            eventBus.subscribe<WorldBuilderEvent.TerrainSelected> { event -> hudView.selectTerrain(event.terrainId) },
            eventBus.subscribe<WorldBuilderEvent.TilePainted> { event -> displayPaintedTile(event.row, event.column) },
            eventBus.subscribe<WorldBuilderEvent.TilePaintRejected> { event ->
                infoView.displayError(forbiddenAdjacentTerrainsMessage(event.terrainId, event.adjacentTerrainId))
            },
            eventBus.subscribe<WorldBuilderEvent.WorldImported> {
                displayWorld()
                infoView.displayInfo(texts[UiText.WORLD_BUILDER_WORLD_IMPORTED])
            },
            eventBus.subscribe<WorldBuilderEvent.WorldImportRejected> { event -> infoView.displayError(importRejectedMessage(event)) },
        )

    init {
        battlefieldView.setDelegate(this)
        hudView.setDelegate(this)
        menuView.displayOptions(listOf(WorldBuilderMenuView.Option(label = texts[UiText.WORLD_BUILDER_IMPORT]) { importWorld() }))
        displayExportAndBack()
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    private fun displayWorld() {
        val world = worldBuilderApi.searchWorld() ?: return
        battlefieldView.displayBattlefield(BattlefieldMapper.terrain(battlefieldApi.previewBattlefield(world.tiles)))
        infoView.displaySize(rows = world.rows, columns = world.columns)
        hudView.displaySize(rows = world.rows, columns = world.columns, allowedSizes = WorldBuilder.MINIMUM_SIZE..WorldBuilder.MAXIMUM_SIZE)
    }

    private fun displayTerrains() {
        val world = worldBuilderApi.searchWorld() ?: return
        val terrains =
            terrainApi.searchTerrains().map { terrain ->
                WorldBuilderHudView.TerrainOption(terrainId = terrain.id, bitmap = battlefieldView.terrainTileBitmap(terrain.id, terrainTransition = null))
            }
        hudView.displayTerrains(terrains, selectedTerrainId = world.selectedTerrainId)
    }

    // The tiles adjacent to the painted tile are drawn again too, as their transitions to it may have changed.
    private fun displayPaintedTile(
        row: Int,
        column: Int,
    ) {
        val world = worldBuilderApi.searchWorld() ?: return
        val positions =
            listOf(row to column, row - 1 to column, row to column + 1, row + 1 to column, row to column - 1)
                .filter { (tileRow, tileColumn) -> tileRow in 0 until world.rows && tileColumn in 0 until world.columns }
                .map { (tileRow, tileColumn) -> TilePosition(tileRow, tileColumn) }
        battlefieldView.updateTerrain(BattlefieldMapper.terrain(battlefieldApi.previewBattlefield(world.tiles)), positions)
    }

    private fun displayExportAndBack() {
        callToActionView.displayExportAndBack(onExported = { exportWorld() }, onBack = { askToLeave() })
    }

    private fun exportWorld() {
        val toml = worldBuilderApi.exportWorld()
        coroutineScope.launchImmediately {
            clipboard.copy(toml)
            infoView.displayInfo(texts[UiText.WORLD_BUILDER_WORLD_COPIED])
        }
    }

    // Replaces the world with the one in the picked file, e.g.: exported earlier. The current world is lost. The picker is
    // opened before suspending, as browsers only open it while handling the tap.
    private fun importWorld() {
        coroutineScope.launchImmediately {
            val text =
                try {
                    worldFilePicker.pickWorld()
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    infoView.displayError(texts[UiText.WORLD_BUILDER_FILE_NOT_READ])
                    return@launchImmediately
                }
            if (text != null) worldBuilderApi.importWorld(text)
        }
    }

    private fun importRejectedMessage(event: WorldBuilderEvent.WorldImportRejected): String =
        when (event.reason) {
            Reason.NOT_A_WORLD -> texts[UiText.WORLD_BUILDER_NOT_A_WORLD]
            Reason.SIZE_OUT_OF_RANGE ->
                texts[UiText.WORLD_BUILDER_SIZE_OUT_OF_RANGE, "minimum" to WorldBuilder.MINIMUM_SIZE, "maximum" to WorldBuilder.MAXIMUM_SIZE]
            Reason.UNKNOWN_TERRAIN -> texts[UiText.WORLD_BUILDER_UNKNOWN_TERRAIN, "terrain" to event.terrainId.orEmpty()]
            Reason.FORBIDDEN_ADJACENT_TERRAINS -> forbiddenAdjacentTerrainsMessage(event.terrainId!!, event.adjacentTerrainId!!)
        }

    private fun forbiddenAdjacentTerrainsMessage(
        terrainId: String,
        adjacentTerrainId: String,
    ): String = texts[UiText.WORLD_BUILDER_FORBIDDEN_ADJACENT_TERRAINS, "terrain" to texts.terrainName(terrainId), "neighbour" to texts.terrainName(adjacentTerrainId)]

    private fun askToLeave() {
        infoView.displayInfo(texts[UiText.WORLD_BUILDER_WORLD_LOST_WHEN_LEAVING])
        callToActionView.displayLeaveConfirmation(onLeft = onLeft, onStayed = { displayExportAndBack() })
    }

    // Delegates
    override fun tileSelected(
        row: Int,
        column: Int,
    ) = worldBuilderApi.paintTile(row = row, column = column)

    override fun sizeChanged(
        rows: Int,
        columns: Int,
    ) = worldBuilderApi.resizeWorld(rows = rows, columns = columns)

    override fun terrainSelected(terrainId: String) = worldBuilderApi.selectTerrain(terrainId)
}
