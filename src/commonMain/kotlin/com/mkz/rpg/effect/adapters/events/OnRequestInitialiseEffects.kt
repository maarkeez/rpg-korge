package com.mkz.rpg.effect.adapters.events

import com.mkz.rpg.effect.domain.EffectEvent
import com.mkz.rpg.effect.usecases.commands.InitialiseEffects
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitialiseEffects(
    initialiseEffects: InitialiseEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<EffectEvent.RequestInitialiseEffects> {
            initialiseEffects()
        }
}
