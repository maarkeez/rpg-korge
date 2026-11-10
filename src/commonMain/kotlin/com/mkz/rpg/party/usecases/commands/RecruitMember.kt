package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestDeployBattleUnit
import com.mkz.rpg.party.domain.PartyEvent.RequestJoinParty
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

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
