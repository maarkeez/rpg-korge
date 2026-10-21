package com.mkz.rpg.npc.adapters.events

import com.mkz.rpg.npc.usecases.commands.RecruitNpc
import com.mkz.rpg.party.domain.PartyEvent.NpcTalkFinished
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// A recruitable npc joins the party once the talk with it is finished.
class OnNpcTalkFinished(
    recruitNpc: RecruitNpc,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<NpcTalkFinished> { event ->
            recruitNpc(npcId = event.npcId)
        }
}
