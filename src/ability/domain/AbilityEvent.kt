package ability.domain

import shared.domain.DomainEvent

sealed interface AbilityEvent: DomainEvent {
    data class AbilityCreated(
        val abilityId: String,
    ): AbilityEvent
}
