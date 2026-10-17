package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

/** Starts the next round, once the battle units joining the battle (e.g.: the CPU battle units engaging) have joined it. */
class StartNextRound(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(joiningBattleUnitIds: List<String>) {
        val storedBattle = battleRepository.search() ?: return
        val joinedBattle = joiningBattleUnitIds.fold(storedBattle) { battle, battleUnitId -> battle.join(battleUnitId) }
        val (events, battle) = joinedBattle.startNextRound().pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
