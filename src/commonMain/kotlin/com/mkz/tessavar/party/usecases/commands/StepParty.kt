package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battlefield.usecases.queries.SearchOccupant
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.party.domain.Party
import com.mkz.tessavar.party.domain.Party.Dto.DirectionDto
import com.mkz.tessavar.party.domain.Party.Dto.PositionDto
import com.mkz.tessavar.party.domain.Party.Dto.StepDto
import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.party.usecases.services.PartyStepping
import com.mkz.tessavar.shared.domain.EventBus

class StepParty(
    private val partyRepository: PartyRepository,
    private val searchPosition: SearchPosition,
    private val searchOccupant: SearchOccupant,
    private val partyStepping: PartyStepping,
    private val eventBus: EventBus,
) {
    operator fun invoke(direction: DirectionDto) {
        val party = partyRepository.search() ?: return
        if (!party.canStep()) return
        val positions = party.toDto().walkingOrderBattleUnitIds.mapNotNull { battleUnitId -> searchPosition(battleUnitId)?.let { battleUnitId to it } }
        val (leaderId, leaderPosition) = positions.firstOrNull() ?: return
        val leaderStep = StepDto(battleUnitId = leaderId, row = leaderPosition.row + direction.rowOffset, column = leaderPosition.column + direction.columnOffset)
        val follower = searchOccupant(leaderStep.row, leaderStep.column)?.takeIf { occupantId -> occupantId != leaderId && occupantId in party.toDto().battleUnitIds }
        if (follower != null) {
            swapLeader(party, follower, positions.associate { (battleUnitId, position) -> battleUnitId to PositionDto(row = position.row, column = position.column) })
            return
        }
        if (!partyStepping.canReach(battleUnitId = leaderStep.battleUnitId, row = leaderStep.row, column = leaderStep.column)) return
        val followerSteps =
            positions.zipWithNext { (_, aheadPosition), (followerId, _) ->
                StepDto(battleUnitId = followerId, row = aheadPosition.row, column = aheadPosition.column)
            }
        partyStepping.continueStepping(party.stepParty(listOf(leaderStep) + followerSteps))
    }

    private fun swapLeader(
        party: Party,
        followerId: String,
        positions: Map<String, PositionDto>,
    ) {
        val (events, swappingParty) = party.swapLeader(followerId = followerId, positions = positions).pullEvents()
        partyRepository.save(swappingParty)
        eventBus.publish(BattleUnitEvent.RequestSwapBattleUnits(battleUnitId = party.toDto().leaderBattleUnitId!!, otherBattleUnitId = followerId))
        eventBus.publish(events)
    }
}
