package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.party.domain.Party.Dto.PositionDto
import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

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
