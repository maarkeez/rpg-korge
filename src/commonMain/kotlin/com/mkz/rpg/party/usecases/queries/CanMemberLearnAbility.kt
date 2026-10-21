package com.mkz.rpg.party.usecases.queries

import com.mkz.rpg.battleUnit.usecases.queries.CanLearnAbility
import com.mkz.rpg.party.domain.PartyRepository

/** Whether the party member can learn the ability for good. Never for a battle unit out of the party. */
class CanMemberLearnAbility(
    private val partyRepository: PartyRepository,
    private val canLearnAbility: CanLearnAbility,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
    ): Boolean {
        val party = partyRepository.search() ?: return false
        if (battleUnitId !in party.toDto().battleUnitIds) return false
        return canLearnAbility(battleUnitId, abilityId)
    }
}
