package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

class FinishBattle(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val storedBattle = battleRepository.search() ?: return
        if (!storedBattle.isBattleFinished()) return
        val (events, battle) = storedBattle.finishBattle().pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
