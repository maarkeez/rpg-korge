package com.mkz.tessavar.player.adapters.events

import com.mkz.tessavar.player.domain.PlayerEvent
import com.mkz.tessavar.player.usecases.commands.InitialisePlayers
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestInitialisePlayers(
    initialisePlayers: InitialisePlayers,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<PlayerEvent.RequestInitialisePlayers> {
            initialisePlayers()
        }
}
