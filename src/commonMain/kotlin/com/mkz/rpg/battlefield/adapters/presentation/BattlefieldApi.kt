package com.mkz.rpg.battlefield.adapters.presentation

import com.mkz.rpg.battlefield.adapters.events.OnBattleRoundStarted
import com.mkz.rpg.battlefield.adapters.events.OnRequestEffectApplicationToOccupants
import com.mkz.rpg.battlefield.adapters.events.OnRequestInitialiseBattlefield
import com.mkz.rpg.battlefield.adapters.events.OnRequestMoveOccupant
import com.mkz.rpg.battlefield.adapters.events.OnRequestOccupyTile
import com.mkz.rpg.battlefield.adapters.events.OnRequestRemoveOccupant
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.usecases.commands.InitialiseBattlefield
import com.mkz.rpg.battlefield.usecases.commands.RemoveOccupant
import com.mkz.rpg.battlefield.usecases.commands.RequestBattlefieldCreation
import com.mkz.rpg.battlefield.usecases.commands.TriggerTerrainEffects
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTerrainBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.ExportBattlefield
import com.mkz.rpg.battlefield.usecases.queries.ParseBattlefield
import com.mkz.rpg.battlefield.usecases.queries.PreviewBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchShortestPath
import com.mkz.rpg.battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.SearchWalkingDistance
import com.mkz.rpg.battlefield.usecases.services.BattlefieldDecoder
import com.mkz.rpg.battlefield.usecases.services.BattlefieldEncoder
import com.mkz.rpg.battlefield.usecases.services.BattlefieldLoader
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi

class BattlefieldApi(
    terrainApi: TerrainApi,
    eventBus: EventBus,
    private val battlefieldLoader: BattlefieldLoader = ResourcesBattlefieldLoader(),
    battlefieldEncoder: BattlefieldEncoder = TomlBattlefieldEncoder(),
    battlefieldDecoder: BattlefieldDecoder = TomlBattlefieldDecoder(),
) {
    // Storage
    private val battlefieldRepository = InMemoryBattlefieldRepository()

    // Commands
    val requestBattlefieldCreation =
        RequestBattlefieldCreation(
            battlefieldRepository = battlefieldRepository,
            searchTerrainById = terrainApi.searchTerrainById,
            eventBus = eventBus,
        )
    val initialiseBattlefield =
        InitialiseBattlefield(
            battlefieldLoader = battlefieldLoader,
            battlefieldRepository = battlefieldRepository,
            searchTerrainById = terrainApi.searchTerrainById,
            eventBus = eventBus,
        )
    val removeOccupant = RemoveOccupant(battlefieldRepository, eventBus)
    val updateBattlefieldOccupancy = UpdateBattlefieldOccupancy(battlefieldRepository, eventBus)
    val triggerTerrainEffects =
        TriggerTerrainEffects(
            battlefieldRepository = battlefieldRepository,
            searchTerrainById = terrainApi.searchTerrainById,
            eventBus = eventBus,
        )

    // Queries
    val searchBattlefield = SearchBattlefield(battlefieldRepository)
    val searchOccupant = SearchOccupant(battlefieldRepository)
    val canBattlefieldTileBeOccupied = CanBattlefieldTileBeOccupied(battlefieldRepository)
    val canBattlefieldTerrainBeOccupied = CanBattlefieldTerrainBeOccupied(battlefieldRepository)
    val searchTilesThatCanBeOccupied = SearchTilesThatCanBeOccupied(canBattlefieldTileBeOccupied, battlefieldRepository)
    val searchPosition = SearchPosition(battlefieldRepository)
    val searchShortestPath = SearchShortestPath(canBattlefieldTileBeOccupied)
    val searchWalkingDistance = SearchWalkingDistance(canBattlefieldTileBeOccupied)
    val previewBattlefield = PreviewBattlefield(battlefieldRepository, terrainApi.searchTerrainById)
    val exportBattlefield = ExportBattlefield(battlefieldEncoder)
    val parseBattlefield = ParseBattlefield(battlefieldDecoder)

    // Event Listeners
    private val onRequestOccupyTile = OnRequestOccupyTile(updateBattlefieldOccupancy, eventBus)
    private val onRequestMoveOccupant = OnRequestMoveOccupant(updateBattlefieldOccupancy, eventBus)
    private val onRequestRemoveOccupant = OnRequestRemoveOccupant(removeOccupant, eventBus)
    private val onRequestInitialiseBattlefield = OnRequestInitialiseBattlefield(initialiseBattlefield, eventBus)
    private val onBattleRoundStarted = OnBattleRoundStarted(triggerTerrainEffects, eventBus)
    private val onRequestEffectApplicationToOccupants = OnRequestEffectApplicationToOccupants(triggerTerrainEffects, eventBus)

    suspend fun init() {
        battlefieldLoader.initResources()
    }
}
