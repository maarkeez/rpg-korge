package com.mkz.rpg.ability.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface AbilityEvent : DomainEvent {
    data class AbilityCreated(
        val abilityId: String,
    ) : AbilityEvent
}
