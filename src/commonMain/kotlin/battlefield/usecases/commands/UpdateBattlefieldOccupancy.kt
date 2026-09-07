package battlefield.usecases.commands

import battlefield.domain.*
import shared.domain.*

class UpdateBattlefieldOccupancy(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(row: Int, column: Int, battleUnitId: String) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.occupy(row, column, battleUnitId).pullEvents()
        battlefieldRepository.update(battlefield)
        eventBus.publish(events)
    }
}
