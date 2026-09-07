package battlefield.usecases.commands

import battlefield.domain.Battlefield
import battlefield.domain.BattlefieldRepository
import shared.domain.EventBus

class InitializeBattlefield(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(rows: Int, columns: Int, tiles: List<List<String>>) {
        if(battlefieldRepository.search() != null) return
        val (events, battlefield) = Battlefield.create(rows, columns, tiles).pullEvents()
        battlefieldRepository.create(battlefield)
        eventBus.publish(events)
    }
}
