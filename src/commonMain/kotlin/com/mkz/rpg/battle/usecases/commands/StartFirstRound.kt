package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

class StartFirstRound(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(players: List<String>) {
        val (events, battle) = Battle.startFirstRound(players).pullEvents()
        battleRepository.create(battle)
        eventBus.publish(events)
    }
}
