package com.mkz.tessavar.party.usecases.queries

import com.mkz.tessavar.battleUnit.usecases.queries.CanLearnAbility
import com.mkz.tessavar.party.domain.PartyRepository

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
