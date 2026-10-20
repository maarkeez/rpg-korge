package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.party.domain.Party.Dto.StepDto
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.party.usecases.services.PartyWalking

/**
 * Walks the leader one tile towards the direction, and each follower to the tile the battle unit ahead of it left.
 * Nothing happens when the party can not walk (e.g.: it is still walking, or battling) or the leader can not occupy the tile.
 */
class WalkParty(
    private val partyRepository: PartyRepository,
    private val searchPosition: SearchPosition,
    private val partyWalking: PartyWalking,
) {
    operator fun invoke(direction: DirectionDto) {
        val party = partyRepository.search() ?: return
        if (!party.canWalk()) return
        val positions = party.toDto().battleUnitIds.mapNotNull { battleUnitId -> searchPosition(battleUnitId)?.let { battleUnitId to it } }
        val (leaderId, leaderPosition) = positions.firstOrNull() ?: return
        val leaderStep = StepDto(battleUnitId = leaderId, row = leaderPosition.row + direction.rowOffset, column = leaderPosition.column + direction.columnOffset)
        if (!partyWalking.canReach(battleUnitId = leaderStep.battleUnitId, row = leaderStep.row, column = leaderStep.column)) return
        val followerSteps =
            positions.zipWithNext { (_, aheadPosition), (followerId, _) ->
                StepDto(battleUnitId = followerId, row = aheadPosition.row, column = aheadPosition.column)
            }
        partyWalking.continueWalking(party.walkParty(listOf(leaderStep) + followerSteps))
    }
}
