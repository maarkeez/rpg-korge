package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.exploration.domain.Exploration
import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.player.usecases.queries.SearchHumanPlayer
import com.mkz.rpg.shared.domain.EventBus

/** Starts exploring with the battle units of the human player: the first one leads, and the rest follow in their order. */
class StartExploration(
    private val searchHumanPlayer: SearchHumanPlayer,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val explorationRepository: ExplorationRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        if (explorationRepository.search() != null) return
        val humanPlayer = searchHumanPlayer() ?: return
        val battleUnitIds = searchBattleUnitsByPlayerId(humanPlayer.id).map { battleUnit -> battleUnit.id }
        if (battleUnitIds.isEmpty()) return
        val (events, exploration) = Exploration.start(playerId = humanPlayer.id, battleUnitIds = battleUnitIds).pullEvents()
        explorationRepository.save(exploration)
        eventBus.publish(events)
    }
}
