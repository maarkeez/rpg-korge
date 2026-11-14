package com.mkz.tessavar.battle.usecases.queries

import com.mkz.tessavar.battle.domain.Battle
import com.mkz.tessavar.battle.domain.BattleRepository

class SearchBattle(
    val battleRepository: BattleRepository,
) {
    operator fun invoke(): Battle.Dto? = battleRepository.search()?.toDto()
}
