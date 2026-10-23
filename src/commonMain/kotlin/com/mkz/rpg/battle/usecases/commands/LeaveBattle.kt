package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Removes the battle unit from the battle, when there is one, e.g.: when it is defeated. The battle is disengaged when
 * the battle unit was the last one of its player in it, e.g.: the last CPU battle unit engaged.
 */
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
