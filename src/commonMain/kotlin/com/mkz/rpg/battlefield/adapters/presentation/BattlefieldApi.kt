package com.mkz.rpg.battlefield.adapters.presentation

import com.mkz.rpg.battlefield.adapters.events.OnBattleRoundStarted
import com.mkz.rpg.battlefield.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.battlefield.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.battlefield.adapters.events.OnBattleUnitMoved
import com.mkz.rpg.battlefield.adapters.events.OnItemStored
import com.mkz.rpg.battlefield.adapters.events.OnLootDropped
import com.mkz.rpg.battlefield.adapters.events.OnRequestEffectApplicationToOccupants
import com.mkz.rpg.battlefield.adapters.events.OnRequestInitialiseBattlefield
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.usecases.commands.FindLoot
import com.mkz.rpg.battlefield.usecases.commands.InitialiseBattlefield
import com.mkz.rpg.battlefield.usecases.commands.PickUpDroppedItem
import com.mkz.rpg.battlefield.usecases.commands.PlaceDroppedItem
import com.mkz.rpg.battlefield.usecases.commands.RemoveOccupant
import com.mkz.rpg.battlefield.usecases.commands.RequestBattlefieldCreation
import com.mkz.rpg.battlefield.usecases.commands.RequestEffectApplicationToOccupants
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTerrainBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.ExportBattlefield
import com.mkz.rpg.battlefield.usecases.queries.ParseBattlefield
import com.mkz.rpg.battlefield.usecases.queries.PreviewBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchDroppedItems
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
    val requestEffectApplicationToOccupants =
        RequestEffectApplicationToOccupants(
            battlefieldRepository = battlefieldRepository,
            searchTerrainById = terrainApi.searchTerrainById,
            eventBus = eventBus,
        )
    val placeDroppedItem = PlaceDroppedItem(battlefieldRepository, eventBus)
    val findLoot = FindLoot(battlefieldRepository, eventBus)
    val pickUpDroppedItem = PickUpDroppedItem(battlefieldRepository, eventBus)

    // Queries
    val searchBattlefield = SearchBattlefield(battlefieldRepository)
    val searchOccupant = SearchOccupant(battlefieldRepository)
    val canBattlefieldTileBeOccupied = CanBattlefieldTileBeOccupied(battlefieldRepository)
    val canBattlefieldTerrainBeOccupied = CanBattlefieldTerrainBeOccupied(battlefieldRepository)
    val searchTilesThatCanBeOccupied = SearchTilesThatCanBeOccupied(canBattlefieldTileBeOccupied, battlefieldRepository)
    val searchPosition = SearchPosition(battlefieldRepository)
    val searchShortestPath = SearchShortestPath(canBattlefieldTileBeOccupied)
    val searchWalkingDistance = SearchWalkingDistance(canBattlefieldTileBeOccupied)
    val searchDroppedItems = SearchDroppedItems(battlefieldRepository)
    val previewBattlefield = PreviewBattlefield(battlefieldRepository, terrainApi.searchTerrainById)
    val exportBattlefield = ExportBattlefield(battlefieldEncoder)
    val parseBattlefield = ParseBattlefield(battlefieldDecoder)

    // Event Listeners
    private val onBattleUnitDeployed = OnBattleUnitDeployed(updateBattlefieldOccupancy, eventBus)
    private val onBattleUnitMoved = OnBattleUnitMoved(updateBattlefieldOccupancy, findLoot, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(removeOccupant, eventBus)
    private val onRequestInitialiseBattlefield = OnRequestInitialiseBattlefield(initialiseBattlefield, eventBus)
    private val onBattleRoundStarted = OnBattleRoundStarted(requestEffectApplicationToOccupants, eventBus)
    private val onRequestEffectApplicationToOccupants = OnRequestEffectApplicationToOccupants(requestEffectApplicationToOccupants, eventBus)
    private val onLootDropped = OnLootDropped(placeDroppedItem, eventBus)
    private val onItemStored = OnItemStored(pickUpDroppedItem, eventBus)

    suspend fun init() {
        battlefieldLoader.initResources()
    }
}
