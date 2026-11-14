package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.domain.TelemetryEvent.RequestRecordGameSaved
import com.mkz.tessavar.telemetry.usecases.commands.RecordGameSaved

class OnRequestRecordGameSaved(
    recordGameSaved: RecordGameSaved,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRecordGameSaved> { event ->
            recordGameSaved(slot = event.slot)
        }
}
