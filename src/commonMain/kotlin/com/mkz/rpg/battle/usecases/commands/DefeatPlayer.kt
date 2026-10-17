package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.battleUnit.usecases.queries.HasAllBattleUnitsDefeated
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.shared.domain.EventBus

/**
 * Defeats the player once all their battle units are defeated, in a battle or while exploring (e.g.: by the terrain).
 * Out of the battles, their enemy is left as the only player, so it wins.
 */
class DefeatPlayer(
    private val hasAllBattleUnitsDefeated: HasAllBattleUnitsDefeated,
    private val searchEnemyPlayer: SearchEnemyPlayer,
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(playerId: String) {
        if (!hasAllBattleUnitsDefeated(playerId)) return
        val storedBattle = battleRepository.search()
        if (storedBattle != null && !storedBattle.toDto().isFinished) {
            val (events, battle) = storedBattle.defeatPlayer(playerId).pullEvents()
            battleRepository.update(battle)
            eventBus.publish(events)
        } else {
            val winner = searchEnemyPlayer(playerId) ?: return
            val (events, battle) = Battle.defeatWhileExploring(defeatedPlayerId = playerId, winnerPlayerId = winner.id).pullEvents()
            battleRepository.create(battle)
            eventBus.publish(events)
        }
    }
}
