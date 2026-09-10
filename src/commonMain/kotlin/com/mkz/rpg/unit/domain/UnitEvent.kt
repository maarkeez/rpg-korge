package com.mkz.rpg.unit.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface UnitEvent : DomainEvent {
    data class UnitCreated(
        val unitId: String,
    ) : UnitEvent
}
