package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.party.usecases.services.PartyStepping

/** Lets the party step continue once the step it awaits has been walked. Other occupied tiles are ignored. */
class ResolvePartyStep(
    private val partyRepository: PartyRepository,
    private val partyStepping: PartyStepping,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val party = partyRepository.search() ?: return
        val resolvedParty = party.resolveStep(battleUnitId = battleUnitId, row = row, column = column)
        if (resolvedParty == party) return
        partyStepping.continueStepping(resolvedParty)
    }
}
