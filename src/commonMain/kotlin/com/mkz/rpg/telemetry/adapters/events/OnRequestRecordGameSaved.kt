package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.domain.TelemetryEvent.RequestRecordGameSaved
import com.mkz.rpg.telemetry.usecases.commands.RecordGameSaved

class OnRequestRecordGameSaved(
    recordGameSaved: RecordGameSaved,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRecordGameSaved> { event ->
            recordGameSaved(slot = event.slot)
        }
}
