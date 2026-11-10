package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository

class LeaveParty(
    private val partyRepository: PartyRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val party = partyRepository.search() ?: return
        if (battleUnitId !in party.toDto().battleUnitIds) return
        partyRepository.save(party.leave(battleUnitId))
    }
}
