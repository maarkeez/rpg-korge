package com.mkz.rpg.player.adapters.events

import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.player.usecases.commands.InitialisePlayers
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitialisePlayers(
    initialisePlayers: InitialisePlayers,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<PlayerEvent.RequestInitialisePlayers> {
            initialisePlayers()
        }
}
