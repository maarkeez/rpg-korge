package com.mkz.rpg.effect.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface EffectEvent : DomainEvent {
    data class EffectCreated(
        val effectId: String,
    ) : EffectEvent
}
