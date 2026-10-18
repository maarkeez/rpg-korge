package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.npc.usecases.queries.SearchNpcAt
import com.mkz.rpg.shared.domain.EventBus

/**
 * Talks to the first npc standing next to the leader, looking north, east, south and west, in that order: no diagonals,
 * whichever way the leader faces. Nothing happens when the party can not walk (e.g.: it is still walking, or battling)
 * or there is no npc next to the leader.
 */
class TalkToAdjacentNpc(
    private val explorationRepository: ExplorationRepository,
    private val searchPosition: SearchPosition,
    private val searchNpcAt: SearchNpcAt,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val exploration = explorationRepository.search() ?: return
        if (!exploration.canWalk()) return
        val leaderId = exploration.toDto().leaderBattleUnitId ?: return
        val leaderPosition = searchPosition(leaderId) ?: return
        val npc =
            DirectionDto.entries.firstNotNullOfOrNull { direction ->
                searchNpcAt(row = leaderPosition.row + direction.rowOffset, column = leaderPosition.column + direction.columnOffset)
            } ?: return
        val (events, talkingExploration) = exploration.talk(npc.id).pullEvents()
        explorationRepository.save(talkingExploration)
        eventBus.publish(events)
    }
}
