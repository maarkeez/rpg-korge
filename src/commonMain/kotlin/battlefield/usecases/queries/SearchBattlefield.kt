package battlefield.usecases.queries

import battlefield.domain.Battlefield
import battlefield.domain.BattlefieldRepository

class SearchBattlefield(private val battlefieldRepository: BattlefieldRepository) {
    operator fun invoke(): Battlefield.Dto? = battlefieldRepository.search()?.toDto()
}
