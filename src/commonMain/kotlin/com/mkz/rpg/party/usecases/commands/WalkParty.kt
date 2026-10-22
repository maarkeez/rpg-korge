package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.party.domain.Party.Dto.PositionDto
import com.mkz.rpg.party.domain.Party.Dto.StepDto
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.party.usecases.services.PartyWalking
import com.mkz.rpg.shared.domain.EventBus

/**
 * Walks the leader one tile towards the direction, and each follower, in line order, to the tile the battle unit ahead
 * of it left. When a party member stands on that tile, the leader swaps tiles with it instead, and the rest stay.
 * Nothing happens when the party can not walk (e.g.: it is still walking, or battling) or the leader can not occupy the tile.
 */
class WalkParty(
    private val partyRepository: PartyRepository,
    private val searchPosition: SearchPosition,
    private val searchOccupant: SearchOccupant,
    private val partyWalking: PartyWalking,
    private val eventBus: EventBus,
) {
    operator fun invoke(direction: DirectionDto) {
        val party = partyRepository.search() ?: return
        if (!party.canWalk()) return
        val positions = party.toDto().lineBattleUnitIds.mapNotNull { battleUnitId -> searchPosition(battleUnitId)?.let { battleUnitId to it } }
        val (leaderId, leaderPosition) = positions.firstOrNull() ?: return
        val leaderStep = StepDto(battleUnitId = leaderId, row = leaderPosition.row + direction.rowOffset, column = leaderPosition.column + direction.columnOffset)
        val follower = searchOccupant(leaderStep.row, leaderStep.column)?.takeIf { occupantId -> occupantId != leaderId && occupantId in party.toDto().battleUnitIds }
        if (follower != null) {
            swapLeader(party, follower, positions.associate { (battleUnitId, position) -> battleUnitId to PositionDto(row = position.row, column = position.column) })
            return
        }
        if (!partyWalking.canReach(battleUnitId = leaderStep.battleUnitId, row = leaderStep.row, column = leaderStep.column)) return
        val followerSteps =
            positions.zipWithNext { (_, aheadPosition), (followerId, _) ->
                StepDto(battleUnitId = followerId, row = aheadPosition.row, column = aheadPosition.column)
            }
        partyWalking.continueWalking(party.walkParty(listOf(leaderStep) + followerSteps))
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
