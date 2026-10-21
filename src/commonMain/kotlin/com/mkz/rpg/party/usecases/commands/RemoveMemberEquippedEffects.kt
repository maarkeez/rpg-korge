package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestRemoveEquippedEffects
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/** Removes the effects an unequipped item applied to a party member. Nothing happens when it is not a party member. */
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
