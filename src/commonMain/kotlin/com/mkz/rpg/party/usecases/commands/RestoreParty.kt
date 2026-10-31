package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.PartyRepository

/**
 * Restores the party a save took, publishing no events: it explores again, done walking. Nothing happens when there was
 * none. Fails when a member is not a battle unit, so the battle units are restored before.
 */
class RestoreParty(
    private val partyRepository: PartyRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
) {
    operator fun invoke(party: Party.Dto.SnapshotDto?) {
        if (party == null) return
        party.battleUnitIds.forEach { battleUnitId -> requireNotNull(searchBattleUnitById(battleUnitId)) { "Unknown battle unit $battleUnitId" } }
        partyRepository.save(Party.restore(party))
    }
}
