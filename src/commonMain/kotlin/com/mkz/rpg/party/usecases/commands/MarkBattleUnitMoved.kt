package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository

class MarkBattleUnitMoved(
    private val partyRepository: PartyRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val party = partyRepository.search() ?: return
        partyRepository.save(party.markMoved(battleUnitId))
    }
}
