package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.battle.usecases.services.BattleLoader
import com.mkz.rpg.shared.domain.EventBus

/** Starts the first round of the battle, with the players playing in the loaded order. */
class InitialiseBattle(
    private val battleLoader: BattleLoader,
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        if (battleRepository.search() != null) return
        val (events, battle) = Battle.startFirstRound(battleLoader.loadPlayerOrder()).pullEvents()
        battleRepository.create(battle)
        eventBus.publish(events)
    }
}
