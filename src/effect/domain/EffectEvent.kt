package effect.domain

import shared.domain.DomainEvent

sealed interface EffectEvent: DomainEvent {
    data class EffectCreated(val effectId: Effect.Dto.Id): EffectEvent
}
