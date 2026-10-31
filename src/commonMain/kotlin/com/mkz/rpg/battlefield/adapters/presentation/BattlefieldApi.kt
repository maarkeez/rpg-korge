package com.mkz.rpg.battlefield.adapters.presentation

import com.mkz.rpg.battlefield.adapters.events.OnBattleRoundStarted
import com.mkz.rpg.battlefield.adapters.events.OnRequestEffectApplicationToOccupants
import com.mkz.rpg.battlefield.adapters.events.OnRequestInitialiseBattlefield
import com.mkz.rpg.battlefield.adapters.events.OnRequestMoveOccupant
import com.mkz.rpg.battlefield.adapters.events.OnRequestOccupyTile
import com.mkz.rpg.battlefield.adapters.events.OnRequestOccupyTiles
import com.mkz.rpg.battlefield.adapters.events.OnRequestRemoveOccupant
import com.mkz.rpg.battlefield.adapters.events.OnRequestSwapOccupants
import com.mkz.rpg.battlefield.adapters.save.BattlefieldSnapshottable
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.usecases.commands.InitialiseBattlefield
import com.mkz.rpg.battlefield.usecases.commands.OccupyTiles
import com.mkz.rpg.battlefield.usecases.commands.RemoveOccupant
import com.mkz.rpg.battlefield.usecases.commands.RequestBattlefieldCreation
import com.mkz.rpg.battlefield.usecases.commands.RestoreBattlefield
import com.mkz.rpg.battlefield.usecases.commands.SwapOccupants
import com.mkz.rpg.battlefield.usecases.commands.TriggerTerrainEffects
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTerrainBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.ExportBattlefield
import com.mkz.rpg.battlefield.usecases.queries.ParseBattlefield
import com.mkz.rpg.battlefield.usecases.queries.PreviewBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefieldSize
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefieldSnapshot
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchShortestPath
import com.mkz.rpg.battlefield.usecases.queries.SearchTerrainId
import com.mkz.rpg.battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.SearchWalkingDistance
import com.mkz.rpg.battlefield.usecases.services.BattlefieldCreation
import com.mkz.rpg.battlefield.usecases.services.BattlefieldDecoder
import com.mkz.rpg.battlefield.usecases.services.BattlefieldEncoder
import com.mkz.rpg.battlefield.usecases.services.BattlefieldLoader
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi

class BattlefieldApi(
    terrainApi: TerrainApi,
    eventBus: EventBus,
    private val battlefieldLoader: BattlefieldLoader,
    battlefieldEncoder: BattlefieldEncoder,
    battlefieldDecoder: BattlefieldDecoder,
) {
    // Storage
    private val battlefieldRepository = InMemoryBattlefieldRepository()

    // Save
    private val searchBattlefieldSnapshot = SearchBattlefieldSnapshot(battlefieldRepository)
    private val restoreBattlefield = RestoreBattlefield(battlefieldRepository)
    val snapshottable: Snapshottable = BattlefieldSnapshottable(searchBattlefieldSnapshot, restoreBattlefield)

    // Services
    private val battlefieldCreation = BattlefieldCreation(battlefieldRepository, terrainApi.searchTerrainById)

    // Commands
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
    val updateBattlefieldOccupancy = UpdateBattlefieldOccupancy(battlefieldRepository, eventBus)
    val occupyTiles = OccupyTiles(battlefieldRepository, eventBus)
    val swapOccupants = SwapOccupants(battlefieldRepository, eventBus)
    val triggerTerrainEffects =
        TriggerTerrainEffects(
            battlefieldRepository = battlefieldRepository,
            searchTerrainById = terrainApi.searchTerrainById,
            eventBus = eventBus,
        )

    // Queries
    val searchBattlefield = SearchBattlefield(battlefieldRepository)
    val searchBattlefieldSize = SearchBattlefieldSize(battlefieldRepository)
    val searchTerrainId = SearchTerrainId(battlefieldRepository)
    val searchOccupant = SearchOccupant(battlefieldRepository)
    val canBattlefieldTileBeOccupied = CanBattlefieldTileBeOccupied(battlefieldRepository)
    val canBattlefieldTerrainBeOccupied = CanBattlefieldTerrainBeOccupied(battlefieldRepository)
    val searchTilesThatCanBeOccupied = SearchTilesThatCanBeOccupied(canBattlefieldTileBeOccupied, battlefieldRepository)
    val searchPosition = SearchPosition(battlefieldRepository)
    val searchShortestPath = SearchShortestPath(canBattlefieldTileBeOccupied)
    val searchWalkingDistance = SearchWalkingDistance(canBattlefieldTileBeOccupied)
    val previewBattlefield = PreviewBattlefield(battlefieldCreation)
    val exportBattlefield = ExportBattlefield(battlefieldEncoder)
    val parseBattlefield = ParseBattlefield(battlefieldDecoder)

    // Event Listeners
    private val onRequestOccupyTile = OnRequestOccupyTile(updateBattlefieldOccupancy, eventBus)
    private val onRequestOccupyTiles = OnRequestOccupyTiles(occupyTiles, eventBus)
    private val onRequestMoveOccupant = OnRequestMoveOccupant(updateBattlefieldOccupancy, eventBus)
    private val onRequestRemoveOccupant = OnRequestRemoveOccupant(removeOccupant, eventBus)
    private val onRequestSwapOccupants = OnRequestSwapOccupants(swapOccupants, eventBus)
    private val onRequestInitialiseBattlefield = OnRequestInitialiseBattlefield(initialiseBattlefield, eventBus)
    private val onBattleRoundStarted = OnBattleRoundStarted(triggerTerrainEffects, eventBus)
    private val onRequestEffectApplicationToOccupants = OnRequestEffectApplicationToOccupants(triggerTerrainEffects, eventBus)

    suspend fun init() {
        battlefieldLoader.initResources()
    }
}
