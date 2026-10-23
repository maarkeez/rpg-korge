package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

/** Finishes the current player turn. Nothing happens once the battle is finished, e.g.: during the CPU turn, as its last battle unit engaged was defeated. */
class FinishPlayerTurn(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val storedBattle = battleRepository.search() ?: return
        if (storedBattle.toDto().isFinished) return
        val (events, battle) = storedBattle.finishPlayerTurn().pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
