package com.mkz.tessavar.npc.adapters.events

import com.mkz.tessavar.npc.usecases.commands.RecruitNpc
import com.mkz.tessavar.party.domain.PartyEvent.NpcTalkFinished
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnNpcTalkFinished(
    recruitNpc: RecruitNpc,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<NpcTalkFinished> { event ->
            recruitNpc(npcId = event.npcId)
        }
}
