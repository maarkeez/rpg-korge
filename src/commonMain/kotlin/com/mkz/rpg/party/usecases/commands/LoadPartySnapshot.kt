package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.PartyRepository

class LoadPartySnapshot(
    private val partyRepository: PartyRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
) {
    operator fun invoke(party: Party.Dto.SnapshotDto?) {
        if (party == null) return
        party.battleUnitIds.forEach { battleUnitId -> requireNotNull(searchBattleUnitById(battleUnitId)) { "Unknown battle unit $battleUnitId" } }
        partyRepository.save(Party.load(party))
    }
}
