package battlefield.usecases.commands

import battlefield.domain.Battlefield
import battlefield.domain.BattlefieldPublisher
import battlefield.domain.BattlefieldRepository

class InitializeBattlefield(
    private val battlefieldRepository: BattlefieldRepository,
    private val battlefieldPublisher: BattlefieldPublisher,
) {
    operator fun invoke(rows: Int, columns: Int, tiles: List<List<String>>) {
        if(battlefieldRepository.search() != null) return
        val (events, battlefield) = Battlefield.create(rows, columns, tiles).pullEvents()
        battlefieldRepository.create(battlefield)
        battlefieldPublisher.publish(events)
    }
}
