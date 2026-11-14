package com.mkz.tessavar.npc.usecases.commands

import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.npc.usecases.queries.SearchAdjacentNpc
import com.mkz.tessavar.party.domain.PartyEvent.RequestStartTalk
import com.mkz.tessavar.party.usecases.queries.SearchParty
import com.mkz.tessavar.shared.domain.EventBus

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
