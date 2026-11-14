package com.mkz.tessavar.battlefield.adapters.presentation

import com.mkz.tessavar.battlefield.adapters.events.OnRequestInitialiseBattlefield
import com.mkz.tessavar.battlefield.adapters.events.OnRequestMoveOccupant
import com.mkz.tessavar.battlefield.adapters.events.OnRequestOccupyTile
import com.mkz.tessavar.battlefield.adapters.events.OnRequestOccupyTiles
import com.mkz.tessavar.battlefield.adapters.events.OnRequestRemoveOccupant
import com.mkz.tessavar.battlefield.adapters.events.OnRequestSwapOccupants
import com.mkz.tessavar.battlefield.adapters.events.OnRequestTerrainEffects
import com.mkz.tessavar.battlefield.adapters.events.OnRoundStarted
import com.mkz.tessavar.battlefield.adapters.save.BattlefieldSnapshottable
import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.usecases.commands.ApplyTerrainEffects
import com.mkz.tessavar.battlefield.usecases.commands.InitialiseBattlefield
import com.mkz.tessavar.battlefield.usecases.commands.LoadBattlefieldSnapshot
import com.mkz.tessavar.battlefield.usecases.commands.OccupyTile
import com.mkz.tessavar.battlefield.usecases.commands.OccupyTiles
import com.mkz.tessavar.battlefield.usecases.commands.RemoveOccupant
import com.mkz.tessavar.battlefield.usecases.commands.RequestBattlefieldCreation
import com.mkz.tessavar.battlefield.usecases.commands.SwapOccupants
import com.mkz.tessavar.battlefield.usecases.queries.ExportBattlefield
import com.mkz.tessavar.battlefield.usecases.queries.IsTileOccupiable
import com.mkz.tessavar.battlefield.usecases.queries.IsTileVacant
import com.mkz.tessavar.battlefield.usecases.queries.ParseBattlefield
import com.mkz.tessavar.battlefield.usecases.queries.PreviewBattlefield
import com.mkz.tessavar.battlefield.usecases.queries.SearchBattlefield
import com.mkz.tessavar.battlefield.usecases.queries.SearchBattlefieldSize
import com.mkz.tessavar.battlefield.usecases.queries.SearchBattlefieldSnapshot
import com.mkz.tessavar.battlefield.usecases.queries.SearchOccupant
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.battlefield.usecases.queries.SearchShortestPath
import com.mkz.tessavar.battlefield.usecases.queries.SearchTerrainId
import com.mkz.tessavar.battlefield.usecases.queries.SearchVacantTiles
import com.mkz.tessavar.battlefield.usecases.queries.SearchWalkingDistance
import com.mkz.tessavar.battlefield.usecases.services.BattlefieldCreation
import com.mkz.tessavar.battlefield.usecases.services.BattlefieldDecoder
import com.mkz.tessavar.battlefield.usecases.services.BattlefieldEncoder
import com.mkz.tessavar.battlefield.usecases.services.BattlefieldLoader
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Snapshottable
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi

class BattlefieldApi(
    terrainApi: TerrainApi,
    eventBus: EventBus,
    private val battlefieldLoader: BattlefieldLoader,
    battlefieldEncoder: BattlefieldEncoder,
    battlefieldDecoder: BattlefieldDecoder,
) {
    private val battlefieldRepository = InMemoryBattlefieldRepository()

    private val searchBattlefieldSnapshot = SearchBattlefieldSnapshot(battlefieldRepository)
    private val loadBattlefieldSnapshot = LoadBattlefieldSnapshot(battlefieldRepository)
    val snapshottable: Snapshottable = BattlefieldSnapshottable(searchBattlefieldSnapshot, loadBattlefieldSnapshot)

    private val battlefieldCreation = BattlefieldCreation(battlefieldRepository, terrainApi.searchTerrainById)

    val requestBattlefieldCreation =
        RequestBattlefieldCreation(
            battlefieldCreation = battlefieldCreation,
            eventBus = eventBus,
        )
    val initialiseBattlefield =
        InitialiseBattlefield(
            battlefieldLoader = battlefieldLoader,
            battlefieldCreation = battlefieldCreation,
            eventBus = eventBus,
        )
    val removeOccupant = RemoveOccupant(battlefieldRepository, eventBus)
    val occupyTile = OccupyTile(battlefieldRepository, eventBus)
    val occupyTiles = OccupyTiles(battlefieldRepository, eventBus)
    val swapOccupants = SwapOccupants(battlefieldRepository, eventBus)
    val applyTerrainEffects =
        ApplyTerrainEffects(
            battlefieldRepository = battlefieldRepository,
            searchTerrainById = terrainApi.searchTerrainById,
            eventBus = eventBus,
        )

    val searchBattlefield = SearchBattlefield(battlefieldRepository)
    val searchBattlefieldSize = SearchBattlefieldSize(battlefieldRepository)
    val searchTerrainId = SearchTerrainId(battlefieldRepository)
    val searchOccupant = SearchOccupant(battlefieldRepository)
    val isTileVacant = IsTileVacant(battlefieldRepository)
    val isTileOccupiable = IsTileOccupiable(battlefieldRepository)
    val searchVacantTiles = SearchVacantTiles(isTileVacant, battlefieldRepository)
    val searchPosition = SearchPosition(battlefieldRepository)
    val searchShortestPath = SearchShortestPath(isTileVacant)
    val searchWalkingDistance = SearchWalkingDistance(isTileVacant)
    val previewBattlefield = PreviewBattlefield(battlefieldCreation)
    val exportBattlefield = ExportBattlefield(battlefieldEncoder)
    val parseBattlefield = ParseBattlefield(battlefieldDecoder)

    private val onRequestOccupyTile = OnRequestOccupyTile(occupyTile, eventBus)
    private val onRequestOccupyTiles = OnRequestOccupyTiles(occupyTiles, eventBus)
    private val onRequestMoveOccupant = OnRequestMoveOccupant(occupyTile, eventBus)
    private val onRequestRemoveOccupant = OnRequestRemoveOccupant(removeOccupant, eventBus)
    private val onRequestSwapOccupants = OnRequestSwapOccupants(swapOccupants, eventBus)
    private val onRequestInitialiseBattlefield = OnRequestInitialiseBattlefield(initialiseBattlefield, eventBus)
    private val onRoundStarted = OnRoundStarted(applyTerrainEffects, eventBus)
    private val onRequestTerrainEffects = OnRequestTerrainEffects(applyTerrainEffects, eventBus)

    suspend fun init() {
        battlefieldLoader.initResources()
    }
}
