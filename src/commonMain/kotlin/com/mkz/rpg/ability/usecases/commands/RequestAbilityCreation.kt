package com.mkz.rpg.ability.usecases.commands

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.usecases.services.AbilityCreation
import com.mkz.rpg.shared.domain.EventBus

class RequestAbilityCreation(
    private val abilityCreation: AbilityCreation,
    private val eventBus: EventBus,
) {
    operator fun invoke(abilityDto: Ability.Dto) {
        eventBus.publish(abilityCreation.create(abilityDto))
    }
}
