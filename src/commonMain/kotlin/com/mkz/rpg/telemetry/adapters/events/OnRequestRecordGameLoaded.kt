package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.domain.TelemetryEvent.RequestRecordGameLoaded
import com.mkz.rpg.telemetry.usecases.commands.RecordGameLoaded

class OnRequestRecordGameLoaded(
    recordGameLoaded: RecordGameLoaded,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRecordGameLoaded> { event ->
            recordGameLoaded(slot = event.slot)
        }
}
