package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository

/** Removes the battle unit from the party, when it is in it, e.g.: when it is defeated. The next one leads when it was leading. */
class LeaveParty(
    private val partyRepository: PartyRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val party = partyRepository.search() ?: return
        if (battleUnitId !in party.toDto().battleUnitIds) return
        partyRepository.save(party.leave(battleUnitId))
    }
}
