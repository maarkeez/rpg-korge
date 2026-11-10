package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.npc.usecases.queries.SearchAdjacentNpc
import com.mkz.rpg.party.domain.PartyEvent.RequestStartTalk
import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.shared.domain.EventBus

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
