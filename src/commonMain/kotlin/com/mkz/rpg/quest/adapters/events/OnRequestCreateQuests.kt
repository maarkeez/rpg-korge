package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.quest.domain.QuestEvent.RequestCreateQuests
import com.mkz.rpg.quest.usecases.commands.CreateQuests
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestCreateQuests(
    createQuests: CreateQuests,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestCreateQuests> {
            createQuests()
        }
}
