package com.mkz.rpg.battlefield.adapters.presentation

import com.mkz.rpg.battlefield.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.battlefield.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.battlefield.adapters.events.OnBattleUnitMoved
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.usecases.commands.InitializeBattlefield
import com.mkz.rpg.battlefield.usecases.commands.RemoveOccupant
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import com.mkz.rpg.shared.domain.EventBus

class BattlefieldApi(
    eventBus: EventBus,
) {
    // Storage
    private val battlefieldRepository = InMemoryBattlefieldRepository()

    // Commands
    val initializeBattlefield = InitializeBattlefield(battlefieldRepository, eventBus)
    val removeOccupant = RemoveOccupant(battlefieldRepository, eventBus)
    val updateBattlefieldOccupancy = UpdateBattlefieldOccupancy(battlefieldRepository, eventBus)

    // Queries
    val searchBattlefield = SearchBattlefield(battlefieldRepository)
    val searchOccupant = SearchOccupant(battlefieldRepository)
    val canBattlefieldTileBeOccupied = CanBattlefieldTileBeOccupied(battlefieldRepository)
    val searchTilesThatCanBeOccupied = SearchTilesThatCanBeOccupied(battlefieldRepository)
    val searchPosition = SearchPosition(battlefieldRepository)

    // Event Listeners
    private val onBattleUnitDeployed = OnBattleUnitDeployed(updateBattlefieldOccupancy, eventBus)
    private val onBattleUnitMoved = OnBattleUnitMoved(updateBattlefieldOccupancy, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(removeOccupant, eventBus)
}
