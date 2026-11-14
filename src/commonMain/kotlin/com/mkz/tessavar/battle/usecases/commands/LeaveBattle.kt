package com.mkz.tessavar.battle.usecases.commands

import com.mkz.tessavar.battle.domain.BattleRepository
import com.mkz.tessavar.shared.domain.EventBus

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
