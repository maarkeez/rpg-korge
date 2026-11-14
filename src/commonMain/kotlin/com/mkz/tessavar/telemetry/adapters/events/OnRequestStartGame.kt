package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.domain.TelemetryEvent.RequestStartGame
import com.mkz.tessavar.telemetry.usecases.commands.StartGame

class OnRequestStartGame(
    startGame: StartGame,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestStartGame> {
            startGame()
        }
}
