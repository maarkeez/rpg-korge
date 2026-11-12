package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.domain.TelemetryEvent.RequestStartGame
import com.mkz.rpg.telemetry.usecases.commands.StartGame

class OnRequestStartGame(
    startGame: StartGame,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestStartGame> {
            startGame()
        }
}
