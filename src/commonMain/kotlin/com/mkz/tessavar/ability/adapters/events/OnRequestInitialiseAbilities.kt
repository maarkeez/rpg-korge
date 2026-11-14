package com.mkz.tessavar.ability.adapters.events

import com.mkz.tessavar.ability.domain.AbilityEvent
import com.mkz.tessavar.ability.usecases.commands.InitialiseAbilities
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestInitialiseAbilities(
    initialiseAbilities: InitialiseAbilities,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<AbilityEvent.RequestInitialiseAbilities> {
            initialiseAbilities()
        }
}
