package com.mkz.tessavar.unit.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface UnitEvent : DomainEvent {
    data class UnitCreated(
        val unitId: String,
    ) : UnitEvent

    data class RequestUnitCreation(
        val unitDto: Unit.Dto,
    ) : UnitEvent

    object RequestInitialiseUnits : UnitEvent
}
