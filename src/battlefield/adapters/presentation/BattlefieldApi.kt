package battlefield.adapters.presentation

import battlefield.adapters.storage.InMemoryBattlefieldRepository
import battlefield.usecases.commands.InitializeBattlefield
import battlefield.usecases.commands.RemoveOccupant
import battlefield.usecases.commands.UpdateBattlefieldOccupancy
import battlefield.usecases.queries.SearchBattlefield
import shared.domain.EventBus

class BattlefieldApi(eventBus: EventBus) {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    val initializeBattlefield = InitializeBattlefield(battlefieldRepository,eventBus)
    val removeOccupant = RemoveOccupant(battlefieldRepository, eventBus)
    val updateBattlefieldOccupancy = UpdateBattlefieldOccupancy(battlefieldRepository, eventBus)
    val searchBattlefield = SearchBattlefield(battlefieldRepository)
}
