package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent

class RecordMemberRecruited(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke(unitId: String) {
        recordProductEvent { telemetry, occurredAt -> telemetry.memberRecruited(unitId, occurredAt) }
    }
}
