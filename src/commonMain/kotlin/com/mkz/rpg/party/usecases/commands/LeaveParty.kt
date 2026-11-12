package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

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
