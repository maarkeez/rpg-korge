package com.mkz.tessavar.effect.usecases.commands

import com.mkz.tessavar.effect.domain.Effect
import com.mkz.tessavar.effect.domain.EffectRepository
import com.mkz.tessavar.shared.domain.EventBus

class RequestEffectCreation(
    private val effectRepository: EffectRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(effectDto: Effect.Dto) {
        if (effectRepository.searchById(effectDto.id) != null) return
        val (events, effect) = Effect.create(effectDto).pullEvents()
        effectRepository.create(effect)
        eventBus.publish(events)
    }
}
