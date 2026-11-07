package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.npc.usecases.queries.SearchAdjacentNpc
import com.mkz.rpg.party.domain.PartyEvent.RequestStartTalk
import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.shared.domain.EventBus

/**
 * Requests the party to talk to the first npc adjacent to its leader, looking north, east, south and west, in that
 * order: no diagonals, whichever way the leader faces. Nothing happens when there is no npc adjacent to the leader. The
 * party ignores the request when it can not walk (e.g.: it is still walking, or battling).
 */
class TalkToAdjacentNpc(
    private val searchParty: SearchParty,
    private val searchPosition: SearchPosition,
    private val searchAdjacentNpc: SearchAdjacentNpc,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val leaderId = searchParty()?.leaderBattleUnitId ?: return
        val leaderPosition = searchPosition(leaderId) ?: return
        val npc = searchAdjacentNpc(row = leaderPosition.row, column = leaderPosition.column) ?: return
        eventBus.publish(RequestStartTalk(npcId = npc.id))
    }
}
