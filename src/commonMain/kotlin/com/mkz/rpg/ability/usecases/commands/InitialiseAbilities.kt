package com.mkz.rpg.ability.usecases.commands

import com.mkz.rpg.ability.domain.AbilityRepository
import com.mkz.rpg.ability.usecases.services.AbilityCreation
import com.mkz.rpg.ability.usecases.services.AbilityLoader
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.EventBus

class InitialiseAbilities(
    private val abilityLoader: AbilityLoader,
    abilityRepository: AbilityRepository,
    searchEffectById: SearchEffectById,
    private val eventBus: EventBus,
) {
    private val abilityCreation = AbilityCreation(abilityRepository, searchEffectById)

    operator fun invoke() {
        val events = abilityLoader.loadAbilities().flatMap { abilityDto -> abilityCreation.create(abilityDto) }.toSet()
        eventBus.publish(events)
    }
}
