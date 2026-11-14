package com.mkz.tessavar.quest.adapters.events

import com.mkz.tessavar.party.domain.PartyEvent.NpcTalkFinished
import com.mkz.tessavar.quest.usecases.commands.ResolveNpcTalk
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnNpcTalkFinished(
    resolveNpcTalk: ResolveNpcTalk,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<NpcTalkFinished> { event ->
            resolveNpcTalk(npcId = event.npcId)
        }
}
