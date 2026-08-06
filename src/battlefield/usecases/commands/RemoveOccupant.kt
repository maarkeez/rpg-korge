package battlefield.usecases.commands

import battlefield.domain.Battlefield
import battlefield.domain.BattlefieldPublisher
import battlefield.domain.BattlefieldRepository

class RemoveOccupant(
    private val battlefieldRepository: BattlefieldRepository,
    private val battlefieldPublisher: BattlefieldPublisher,
) {
    operator fun invoke(battleUnitId: String) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.removeOccupant(battleUnitId).pullEvents()
        battlefieldRepository.create(battlefield)
        battlefieldPublisher.publish(events)
    }
}
