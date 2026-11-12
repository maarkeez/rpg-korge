package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.party.domain.PartyEvent.GameOver
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.usecases.commands.RecordGameOver

class OnGameOver(
    recordGameOver: RecordGameOver,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<GameOver> {
            recordGameOver()
        }
}
