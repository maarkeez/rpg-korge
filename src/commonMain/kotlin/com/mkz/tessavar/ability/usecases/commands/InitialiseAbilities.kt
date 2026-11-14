package com.mkz.tessavar.ability.usecases.commands

import com.mkz.tessavar.ability.usecases.services.AbilityCreation
import com.mkz.tessavar.ability.usecases.services.AbilityLoader
import com.mkz.tessavar.shared.domain.EventBus

class InitialiseAbilities(
    private val abilityLoader: AbilityLoader,
    private val abilityCreation: AbilityCreation,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = abilityLoader.loadAbilities().flatMap { abilityDto -> abilityCreation.create(abilityDto) }.toSet()
        eventBus.publish(events)
    }
}
