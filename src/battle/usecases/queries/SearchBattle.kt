package battle.usecases.queries

import battle.domain.Battle
import battle.domain.BattleRepository

class SearchBattle(
    val battleRepository: BattleRepository
) {
    operator fun invoke(): Battle.Dto? =battleRepository.search()?.toDto()
}
