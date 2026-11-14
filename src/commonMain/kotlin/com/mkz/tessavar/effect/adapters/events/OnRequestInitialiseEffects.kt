package com.mkz.tessavar.effect.adapters.events

import com.mkz.tessavar.effect.domain.EffectEvent
import com.mkz.tessavar.effect.usecases.commands.InitialiseEffects
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestInitialiseEffects(
    initialiseEffects: InitialiseEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<EffectEvent.RequestInitialiseEffects> {
            initialiseEffects()
        }
}
