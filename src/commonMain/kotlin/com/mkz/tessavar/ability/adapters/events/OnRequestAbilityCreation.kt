package com.mkz.tessavar.ability.adapters.events

import com.mkz.tessavar.ability.domain.AbilityEvent
import com.mkz.tessavar.ability.usecases.commands.RequestAbilityCreation
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestAbilityCreation(
    private val requestAbilityCreation: RequestAbilityCreation,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<AbilityEvent.RequestAbilityCreation> { event ->
            requestAbilityCreation(event.abilityDto)
        }
}
