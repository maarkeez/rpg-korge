package com.mkz.rpg.effect.usecases.commands

import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.EffectEvent
import com.mkz.rpg.effect.domain.EffectRepository
import com.mkz.rpg.effect.usecases.services.EffectLoader
import com.mkz.rpg.shared.domain.EventBus

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
