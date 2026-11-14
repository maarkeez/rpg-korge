package com.mkz.tessavar.ability.usecases.commands

import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.usecases.services.AbilityCreation
import com.mkz.tessavar.shared.domain.EventBus

class RequestAbilityCreation(
    private val abilityCreation: AbilityCreation,
    private val eventBus: EventBus,
) {
    operator fun invoke(abilityDto: Ability.Dto) {
        eventBus.publish(abilityCreation.create(abilityDto))
    }
}
