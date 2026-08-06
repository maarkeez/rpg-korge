package battlefield.usecases.commands

import battlefield.domain.Battlefield
import battlefield.domain.BattlefieldPublisher
import battlefield.domain.BattlefieldRepository

class UpdateBattlefieldOccupancy(
    private val battlefieldRepository: BattlefieldRepository,
    private val battlefieldPublisher: BattlefieldPublisher,
) {
    operator fun invoke(row: Int, column: Int, battleUnitId: String) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.occupy(row, column, battleUnitId).pullEvents()
        battlefieldRepository.create(battlefield)
        battlefieldPublisher.publish(events)
    }
}
