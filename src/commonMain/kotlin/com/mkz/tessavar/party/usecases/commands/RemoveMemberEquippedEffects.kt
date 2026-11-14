package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.RequestRemoveEquippedEffects
import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

class RemoveMemberEquippedEffects(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        val party = partyRepository.search() ?: return
        if (battleUnitId !in party.toDto().battleUnitIds) return
        eventBus.publish(RequestRemoveEquippedEffects(battleUnitId = battleUnitId, lootableItemId = lootableItemId))
    }
}
