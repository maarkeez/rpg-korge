package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

class LeaveParty(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitId: String) {
        val storedParty = partyRepository.search() ?: return
        if (battleUnitId !in storedParty.toDto().battleUnitIds) return
        val (events, party) = storedParty.leave(battleUnitId).pullEvents()
        partyRepository.save(party)
        eventBus.publish(events)
        if (party.toDto().isOver) eventBus.publish(BattleEvent.RequestFinishBattle)
    }
}
