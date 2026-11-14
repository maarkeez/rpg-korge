package com.mkz.tessavar.effect.usecases.commands

import com.mkz.tessavar.effect.domain.Effect
import com.mkz.tessavar.effect.domain.EffectEvent
import com.mkz.tessavar.effect.domain.EffectRepository
import com.mkz.tessavar.effect.usecases.services.EffectLoader
import com.mkz.tessavar.shared.domain.EventBus

class InitialiseEffects(
    private val effectLoader: EffectLoader,
    private val effectRepository: EffectRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<EffectEvent>()
        effectLoader.loadEffects().forEach { effectDto ->
            if (effectRepository.searchById(effectDto.id) != null) return@forEach
            val (effectEvents, effect) = Effect.create(effectDto).pullEvents()
            effectRepository.create(effect)
            events += effectEvents
        }
        eventBus.publish(events)
    }
}
