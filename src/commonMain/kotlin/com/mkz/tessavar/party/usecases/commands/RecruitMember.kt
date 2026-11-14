package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.RequestDeployBattleUnit
import com.mkz.tessavar.party.domain.PartyEvent.RequestJoinParty
import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

class RecruitMember(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        unitId: String,
        row: Int,
        column: Int,
    ) {
        val party = partyRepository.search() ?: return
        eventBus.publish(
            setOf(
                RequestDeployBattleUnit(battleUnitId = battleUnitId, unitId = unitId, playerId = party.toDto().playerId, row = row, column = column),
                RequestJoinParty(battleUnitId = battleUnitId),
            ),
        )
    }
}
