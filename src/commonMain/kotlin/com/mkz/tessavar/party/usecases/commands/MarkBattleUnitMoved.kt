package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.domain.PartyRepository

class MarkBattleUnitMoved(
    private val partyRepository: PartyRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val party = partyRepository.search() ?: return
        partyRepository.save(party.markMoved(battleUnitId))
    }
}
