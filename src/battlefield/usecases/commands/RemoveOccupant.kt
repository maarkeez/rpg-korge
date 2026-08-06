package battlefield.usecases.commands

import battlefield.domain.Battlefield
import battlefield.domain.BattlefieldRepository
import shared.domain.EventBus

class RemoveOccupant(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitId: String) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.removeOccupant(battleUnitId).pullEvents()
        battlefieldRepository.update(battlefield)
        eventBus.publish(events)
    }
}
