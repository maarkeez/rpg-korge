package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

class LeaveBattle(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitId: String) {
        val storedBattle = battleRepository.search() ?: return
        val (events, battle) = storedBattle.leaveDuringRound(battleUnitId).pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
