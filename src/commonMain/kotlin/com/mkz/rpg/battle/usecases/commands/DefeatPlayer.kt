package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.battleUnit.usecases.queries.HasAllBattleUnitsDefeated
import com.mkz.rpg.shared.domain.EventBus

class DefeatPlayer(
    private val hasAllBattleUnitsDefeated: HasAllBattleUnitsDefeated,
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(playerId: String) {
        val storedBattle = battleRepository.search() ?: return
        if (!hasAllBattleUnitsDefeated(playerId)) return
        val (events, battle) = storedBattle.defeatPlayer(playerId).pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
