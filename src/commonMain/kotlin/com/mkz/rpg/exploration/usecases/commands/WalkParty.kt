package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.exploration.domain.Exploration.Dto.StepDto
import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.exploration.usecases.services.PartyWalking

/**
 * Walks the leader one tile towards the direction, and each follower to the tile the battle unit ahead of it left.
 * Nothing happens when the party can not walk (e.g.: it is still walking, or battling) or the leader can not occupy the tile.
 */
class WalkParty(
    private val explorationRepository: ExplorationRepository,
    private val searchPosition: SearchPosition,
    private val partyWalking: PartyWalking,
) {
    operator fun invoke(direction: DirectionDto) {
        val exploration = explorationRepository.search() ?: return
        if (!exploration.canWalk()) return
        val positions = exploration.toDto().battleUnitIds.mapNotNull { battleUnitId -> searchPosition(battleUnitId)?.let { battleUnitId to it } }
        val (leaderId, leaderPosition) = positions.firstOrNull() ?: return
        val leaderStep = StepDto(battleUnitId = leaderId, row = leaderPosition.row + direction.rowOffset, column = leaderPosition.column + direction.columnOffset)
        if (!partyWalking.canReach(battleUnitId = leaderStep.battleUnitId, row = leaderStep.row, column = leaderStep.column)) return
        val followerSteps =
            positions.zipWithNext { (_, aheadPosition), (followerId, _) ->
                StepDto(battleUnitId = followerId, row = aheadPosition.row, column = aheadPosition.column)
            }
        partyWalking.continueWalking(exploration.walkParty(listOf(leaderStep) + followerSteps))
    }
}
