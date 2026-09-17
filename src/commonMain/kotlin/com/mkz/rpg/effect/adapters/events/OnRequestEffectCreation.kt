package com.mkz.rpg.effect.adapters.events

import com.mkz.rpg.effect.domain.EffectEvent
import com.mkz.rpg.effect.usecases.commands.RequestEffectCreation
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestEffectCreation(
    private val requestEffectCreation: RequestEffectCreation,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<EffectEvent.RequestEffectCreation> { event ->
            requestEffectCreation(event.effectDto)
        }
}
