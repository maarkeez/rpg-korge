package battlefield.usecases.queries

import battlefield.domain.BattlefieldRepository

class SearchOccupant(private val battlefieldRepository: BattlefieldRepository) {
    operator fun invoke(row: Int, column: Int): String? = battlefieldRepository.search()?.occupant(row, column)
}
