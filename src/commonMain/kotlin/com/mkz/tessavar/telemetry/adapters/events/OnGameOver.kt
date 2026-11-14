package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.party.domain.PartyEvent.GameOver
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.usecases.commands.RecordGameOver

class OnGameOver(
    recordGameOver: RecordGameOver,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<GameOver> {
            recordGameOver()
        }
}
