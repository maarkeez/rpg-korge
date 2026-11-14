package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.party.usecases.services.PartyStepping

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
