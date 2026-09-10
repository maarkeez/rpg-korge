package com.mkz.rpg.battle.usecases.queries

import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleRepository

class SearchBattle(
    val battleRepository: BattleRepository,
) {
    operator fun invoke(): Battle.Dto? = battleRepository.search()?.toDto()
}
