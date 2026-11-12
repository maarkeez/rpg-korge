package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.domain.TelemetryEvent.RequestRecordMainMenuOpened
import com.mkz.rpg.telemetry.usecases.commands.RecordMainMenuOpened

class OnRequestRecordMainMenuOpened(
    recordMainMenuOpened: RecordMainMenuOpened,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRecordMainMenuOpened> {
            recordMainMenuOpened()
        }
}
