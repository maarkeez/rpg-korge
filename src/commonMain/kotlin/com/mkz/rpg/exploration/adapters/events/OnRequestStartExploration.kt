package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.exploration.usecases.commands.StartExploration
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestStartExploration(
    startExploration: StartExploration,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<ExplorationEvent.RequestStartExploration> {
            startExploration()
        }
}
