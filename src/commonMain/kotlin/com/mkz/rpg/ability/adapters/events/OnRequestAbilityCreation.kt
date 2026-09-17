package com.mkz.rpg.ability.adapters.events

import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.usecases.commands.RequestAbilityCreation
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestAbilityCreation(
    private val requestAbilityCreation: RequestAbilityCreation,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<AbilityEvent.RequestAbilityCreation> { event ->
            requestAbilityCreation(event.abilityDto)
        }
}
