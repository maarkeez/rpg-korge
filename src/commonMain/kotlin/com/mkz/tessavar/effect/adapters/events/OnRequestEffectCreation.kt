package com.mkz.tessavar.effect.adapters.events

import com.mkz.tessavar.effect.domain.EffectEvent
import com.mkz.tessavar.effect.usecases.commands.RequestEffectCreation
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestEffectCreation(
    private val requestEffectCreation: RequestEffectCreation,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<EffectEvent.RequestEffectCreation> { event ->
            requestEffectCreation(event.effectDto)
        }
}
