package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository

/** Remembers that the battle unit moved during the current second while exploring, so it does not receive the effect of its terrain. */
class MarkBattleUnitMoved(
    private val partyRepository: PartyRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val party = partyRepository.search() ?: return
        partyRepository.save(party.markMoved(battleUnitId))
    }
}
