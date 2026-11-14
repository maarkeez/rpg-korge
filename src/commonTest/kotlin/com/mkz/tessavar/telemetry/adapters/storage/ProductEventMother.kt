package com.mkz.tessavar.telemetry.adapters.storage

import com.mkz.tessavar.telemetry.domain.TelemetryEvent.ProductEventRecorded
import com.mkz.tessavar.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name

object ProductEventMother {
    fun productEventRecorded(
        name: Name = Name.GAME_OVER,
        occurredAt: Double = 1.0,
    ) = ProductEventRecorded(
        name = name,
        installId = "i1",
        sessionId = "s1",
        gameId = "g1",
        occurredAt = occurredAt,
        questId = null,
        slot = null,
        lootableItemId = null,
        price = null,
        unitId = null,
    )
}
