package battlefield.usecases.queries

import battlefield.domain.BattlefieldRepository

class CanBattlefieldTileBeOccupied(private val battlefieldRepository: BattlefieldRepository) {
    operator fun invoke(row: Int, column: Int): Boolean {
        val battlefield = battlefieldRepository.search() ?: return false
        return battlefield.isInBoundaries(row, column) && battlefield.canBeOccupied(row, column)
    }
}
