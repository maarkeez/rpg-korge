package com.mkz.tessavar.battle.usecases.commands

import com.mkz.tessavar.battle.domain.BattleRepository
import com.mkz.tessavar.shared.domain.EventBus

class FinishBattle(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val storedBattle = battleRepository.search() ?: return
        if (storedBattle.toDto().isFinished) return
        val (events, battle) = storedBattle.finish().pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
