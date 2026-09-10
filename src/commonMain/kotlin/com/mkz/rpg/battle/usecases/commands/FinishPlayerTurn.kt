package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

class FinishPlayerTurn(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val storedBattle = battleRepository.search() ?: return
        val (events, battle) = storedBattle.finishPlayerTurn().pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
