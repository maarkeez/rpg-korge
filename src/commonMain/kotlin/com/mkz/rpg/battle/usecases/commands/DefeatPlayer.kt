package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.player.usecases.queries.SearchOpponent
import com.mkz.rpg.shared.domain.EventBus

class DefeatPlayer(
    private val searchOpponent: SearchOpponent,
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(playerId: String) {
        val storedBattle = battleRepository.search()
        if (storedBattle != null && !storedBattle.toDto().isFinished) {
            val (events, battle) = storedBattle.defeatPlayer(playerId).pullEvents()
            battleRepository.update(battle)
            eventBus.publish(events)
        } else {
            val winner = searchOpponent(playerId) ?: return
            val (events, battle) = Battle.defeatWhileExploring(defeatedPlayerId = playerId, winnerPlayerId = winner.id).pullEvents()
            battleRepository.create(battle)
            eventBus.publish(events)
        }
    }
}
