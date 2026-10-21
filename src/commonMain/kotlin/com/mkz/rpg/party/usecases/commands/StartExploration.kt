package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.player.usecases.queries.SearchHumanPlayer
import com.mkz.rpg.shared.domain.EventBus

/** Starts exploring with the battle units of the human player: the first one leads, and the rest follow in their order. */
class StartExploration(
    private val searchHumanPlayer: SearchHumanPlayer,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        if (partyRepository.search() != null) return
        val humanPlayer = searchHumanPlayer() ?: return
        val battleUnits = searchBattleUnitsByPlayerId(humanPlayer.id)
        if (battleUnits.isEmpty()) return
        val (events, party) =
            Party
                .start(
                    playerId = humanPlayer.id,
                    battleUnitIds = battleUnits.map { battleUnit -> battleUnit.id },
                    unitIds = battleUnits.map { battleUnit -> battleUnit.unitId },
                ).pullEvents()
        partyRepository.save(party)
        eventBus.publish(events)
    }
}
