package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.party.domain.Party.Dto.PositionDto
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Closes the formation with the new party order, the leader first, so the party explores again: nobody moves, and the
 * followers line up behind the new leader. Nothing happens when it is not in formation.
 */
class CloseFormation(
    private val partyRepository: PartyRepository,
    private val searchPosition: SearchPosition,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitIds: List<String>) {
        val party = partyRepository.search() ?: return
        val positions =
            battleUnitIds
                .mapNotNull { battleUnitId -> searchPosition(battleUnitId)?.let { position -> battleUnitId to PositionDto(row = position.row, column = position.column) } }
                .toMap()
        val (events, exploringParty) = party.closeFormation(battleUnitIds = battleUnitIds, positions = positions).pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(exploringParty)
        eventBus.publish(events)
    }
}
