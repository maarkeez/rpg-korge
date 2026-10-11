package com.mkz.rpg.ability.adapters.events

import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.usecases.commands.InitialiseAbilities
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitialiseAbilities(
    initialiseAbilities: InitialiseAbilities,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<AbilityEvent.RequestInitialiseAbilities> {
            initialiseAbilities()
        }
}
