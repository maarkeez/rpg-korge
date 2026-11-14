package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.domain.TelemetryEvent.RequestRecordGameLoaded
import com.mkz.tessavar.telemetry.usecases.commands.RecordGameLoaded

class OnRequestRecordGameLoaded(
    recordGameLoaded: RecordGameLoaded,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRecordGameLoaded> { event ->
            recordGameLoaded(slot = event.slot)
        }
}
