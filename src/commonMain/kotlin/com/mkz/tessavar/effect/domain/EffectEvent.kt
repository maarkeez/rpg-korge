package com.mkz.tessavar.effect.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface EffectEvent : DomainEvent {
    data class EffectCreated(
        val effectId: String,
    ) : EffectEvent

    data class RequestEffectCreation(
        val effectDto: Effect.Dto,
    ) : EffectEvent

    object RequestInitialiseEffects : EffectEvent
}
