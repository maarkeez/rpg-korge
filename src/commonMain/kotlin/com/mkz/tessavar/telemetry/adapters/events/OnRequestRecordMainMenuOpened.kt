package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.domain.TelemetryEvent.RequestRecordMainMenuOpened
import com.mkz.tessavar.telemetry.usecases.commands.RecordMainMenuOpened

class OnRequestRecordMainMenuOpened(
    recordMainMenuOpened: RecordMainMenuOpened,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRecordMainMenuOpened> {
            recordMainMenuOpened()
        }
}
