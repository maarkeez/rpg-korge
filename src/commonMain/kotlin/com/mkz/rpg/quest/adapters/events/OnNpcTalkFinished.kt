package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.party.domain.PartyEvent.NpcTalkFinished
import com.mkz.rpg.quest.usecases.commands.ResolveNpcTalk
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnNpcTalkFinished(
    resolveNpcTalk: ResolveNpcTalk,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<NpcTalkFinished> { event ->
            resolveNpcTalk(npcId = event.npcId)
        }
}
