package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.quest.domain.QuestEvent.RequestInitialiseQuests
import com.mkz.rpg.quest.usecases.commands.InitialiseQuests
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitialiseQuests(
    initialiseQuests: InitialiseQuests,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestInitialiseQuests> {
            initialiseQuests()
        }
}
