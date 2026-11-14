package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.usecases.services.RecordProductEvent

class RecordMemberRecruited(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke(unitId: String) {
        recordProductEvent { telemetry, occurredAt -> telemetry.memberRecruited(unitId, occurredAt) }
    }
}
