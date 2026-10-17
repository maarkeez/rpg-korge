package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.mapBuilder.adapters.presentation.MapBuilderApi
import com.mkz.rpg.mapBuilder.domain.MapBuilder
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapImportRejected.Reason
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import korlibs.io.async.launchImmediately
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope

/**
 * Displays the map being designed on the battlefield, as the battle would display it, and lets the player resize it,
 * paint its tiles, export it to the clipboard, import one from a file, and leave once they confirm it.
 */
class MapBuilderPresenter(
    private val battlefieldView: BattlefieldView,
    private val infoView: MapBuilderInfoView,
    private val hudView: MapBuilderHudView,
    private val callToActionView: MapBuilderCallToActionView,
    private val menuView: MapBuilderMenuView,
    private val mapBuilderApi: MapBuilderApi,
    private val battlefieldApi: BattlefieldApi,
    private val terrainApi: TerrainApi,
    private val clipboard: Clipboard,
    private val mapFilePicker: MapFilePicker,
    private val coroutineScope: CoroutineScope,
    private val onLeft: () -> Unit,
    eventBus: EventBus,
) : BattlefieldView.Delegate,
    MapBuilderHudView.Delegate {
    companion object {
        const val MAP_COPIED = "Map copied to the clipboard"
        const val MAP_LOST_WHEN_LEAVING = "The map is lost when leaving"
        const val MAP_IMPORTED = "Map imported"
        const val FILE_NOT_READ = "The file can not be read"
        const val NOT_A_MAP = "The file has no map"
        val MAP_SIZE_OUT_OF_RANGE = "The map must be ${MapBuilder.MINIMUM_SIZE} to ${MapBuilder.MAXIMUM_SIZE} tiles wide and tall"
    }

    private val subscriptions =
        listOf(
            eventBus.subscribe<MapBuilderEvent.MapCreated> {
                displayMap()
                displayTerrains()
            },
            eventBus.subscribe<MapBuilderEvent.MapResized> { displayMap() },
            eventBus.subscribe<MapBuilderEvent.TerrainSelected> { event -> hudView.selectTerrain(event.terrainId) },
            eventBus.subscribe<MapBuilderEvent.TilePainted> { event -> displayPaintedTile(event.row, event.column) },
            eventBus.subscribe<MapBuilderEvent.TilePaintRejected> { event ->
                infoView.displayError(forbiddenNeighboursMessage(event.terrainId, event.neighbourTerrainId))
            },
            eventBus.subscribe<MapBuilderEvent.MapImported> {
                displayMap()
                infoView.displayInfo(MAP_IMPORTED)
            },
            eventBus.subscribe<MapBuilderEvent.MapImportRejected> { event -> infoView.displayError(importRejectedMessage(event)) },
        )

    init {
        battlefieldView.setDelegate(this)
        hudView.setDelegate(this)
        menuView.displayOptions(listOf(MapBuilderMenuView.Option(label = "Import") { importMap() }))
        displayExportAndBack()
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    private fun displayMap() {
        val map = mapBuilderApi.searchMap() ?: return
        battlefieldView.displayBattlefield(battlefieldApi.previewBattlefield(map.tiles))
        infoView.displaySize(rows = map.rows, columns = map.columns)
        hudView.displaySize(rows = map.rows, columns = map.columns)
    }

    private fun displayTerrains() {
        val map = mapBuilderApi.searchMap() ?: return
        val terrains =
            terrainApi.searchTerrains().map { terrain ->
                MapBuilderHudView.TerrainOption(terrainId = terrain.id, bitmap = battlefieldView.terrainTileBitmap(terrain.id, terrainTransition = null))
            }
        hudView.displayTerrains(terrains, selectedTerrainId = map.selectedTerrainId)
    }

    // The neighbours of the painted tile are drawn again too, as their transitions to it may have changed.
    private fun displayPaintedTile(
        row: Int,
        column: Int,
    ) {
        val map = mapBuilderApi.searchMap() ?: return
        val positions =
            listOf(row to column, row - 1 to column, row to column + 1, row + 1 to column, row to column - 1)
                .filter { (tileRow, tileColumn) -> tileRow in 0 until map.rows && tileColumn in 0 until map.columns }
                .map { (tileRow, tileColumn) -> PositionDto(tileRow, tileColumn) }
        battlefieldView.updateTerrain(battlefieldApi.previewBattlefield(map.tiles), positions)
    }

    private fun displayExportAndBack() {
        callToActionView.displayExportAndBack(onExported = { exportMap() }, onBack = { askToLeave() })
    }

    private fun exportMap() {
        val toml = mapBuilderApi.exportMap()
        coroutineScope.launchImmediately {
            clipboard.copy(toml)
            infoView.displayInfo(MAP_COPIED)
        }
    }

    // Replaces the map with the one in the picked file, e.g.: exported earlier. The current map is lost. The picker is
    // opened before suspending, as browsers only open it while handling the tap.
    private fun importMap() {
        coroutineScope.launchImmediately {
            val text =
                try {
                    mapFilePicker.pickMap()
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    infoView.displayError(FILE_NOT_READ)
                    return@launchImmediately
                }
            if (text != null) mapBuilderApi.importMap(text)
        }
    }

    private fun importRejectedMessage(event: MapBuilderEvent.MapImportRejected): String =
        when (event.reason) {
            Reason.NOT_A_MAP -> NOT_A_MAP
            Reason.SIZE_OUT_OF_RANGE -> MAP_SIZE_OUT_OF_RANGE
            Reason.UNKNOWN_TERRAIN -> "Unknown terrain: ${event.terrainId}"
            Reason.FORBIDDEN_NEIGHBOURS -> forbiddenNeighboursMessage(event.terrainId!!, event.neighbourTerrainId!!)
        }

    private fun forbiddenNeighboursMessage(
        terrainId: String,
        neighbourTerrainId: String,
    ): String = "${terrainName(terrainId)} can not be next to ${terrainName(neighbourTerrainId).lowercase()}"

    private fun askToLeave() {
        infoView.displayInfo(MAP_LOST_WHEN_LEAVING)
        callToActionView.displayLeaveConfirmation(onLeft = onLeft, onStayed = { displayExportAndBack() })
    }

    private fun terrainName(terrainId: String): String = terrainId.replaceFirstChar(Char::uppercase)

    // Delegates
    override fun tileSelected(
        row: Int,
        column: Int,
    ) = mapBuilderApi.paintTile(row = row, column = column)

    override fun sizeChanged(
        rows: Int,
        columns: Int,
    ) = mapBuilderApi.resizeMap(rows = rows, columns = columns)

    override fun terrainSelected(terrainId: String) = mapBuilderApi.selectTerrain(terrainId)
}
