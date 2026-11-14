package com.mkz.tessavar.quest.adapters.events

import com.mkz.tessavar.quest.domain.QuestEvent.RequestCreateQuests
import com.mkz.tessavar.quest.usecases.commands.CreateQuests
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestCreateQuests(
    createQuests: CreateQuests,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestCreateQuests> {
            createQuests()
        }
}
