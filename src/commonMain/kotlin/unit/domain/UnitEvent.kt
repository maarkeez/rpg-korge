package unit.domain

import shared.domain.DomainEvent

sealed interface UnitEvent: DomainEvent {
    data class UnitCreated(val unitId: String) : UnitEvent
}
