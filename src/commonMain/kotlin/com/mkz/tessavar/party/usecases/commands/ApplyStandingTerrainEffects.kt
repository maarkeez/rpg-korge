package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.usecases.queries.SearchBattlefield
import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

class ApplyStandingTerrainEffects(
    private val partyRepository: PartyRepository,
    private val searchBattlefield: SearchBattlefield,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        if (!party.toDto().isExploring) return
        val occupantIds =
            searchBattlefield()
                ?.tiles
                ?.values
                ?.mapNotNull { tile -> tile.occupantId }
                ?.filter { occupantId -> searchBattleUnitById(occupantId) != null }
                ?.toSet()
                .orEmpty()
        val standingBattleUnitIds = party.standingBattleUnitIds(occupantIds)
        val (events, nextSecondParty) = party.startNextSecond().pullEvents()
        partyRepository.save(nextSecondParty)
        if (standingBattleUnitIds.isNotEmpty()) {
            eventBus.publish(BattlefieldEvent.RequestTerrainEffects(occupantIds = standingBattleUnitIds))
        }
        eventBus.publish(events)
    }
}
