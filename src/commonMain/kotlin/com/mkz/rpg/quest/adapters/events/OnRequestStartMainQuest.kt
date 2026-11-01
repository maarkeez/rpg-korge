package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.quest.domain.QuestEvent.RequestStartMainQuest
import com.mkz.rpg.quest.usecases.commands.StartMainQuest
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestStartMainQuest(
    startMainQuest: StartMainQuest,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestStartMainQuest> {
            startMainQuest()
        }
}
