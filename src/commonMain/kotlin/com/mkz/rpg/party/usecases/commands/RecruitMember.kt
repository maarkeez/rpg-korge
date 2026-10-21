package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestDeployBattleUnit
import com.mkz.rpg.party.domain.PartyEvent.RequestJoinParty
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Recruits a battle unit of the unit for the party player, e.g.: a recruited npc: it is deployed on the tile, and then
 * joins the party. Both requests are published together, so the battle unit is deployed before it joins. Nothing
 * happens when there is no party.
 */
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
