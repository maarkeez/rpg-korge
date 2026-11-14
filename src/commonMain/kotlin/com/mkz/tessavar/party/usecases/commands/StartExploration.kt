package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.tessavar.party.domain.Party
import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.player.usecases.queries.SearchHumanPlayer
import com.mkz.tessavar.shared.domain.EventBus

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
