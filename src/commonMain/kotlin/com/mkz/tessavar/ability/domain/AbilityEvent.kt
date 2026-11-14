package com.mkz.tessavar.ability.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface AbilityEvent : DomainEvent {
    data class AbilityCreated(
        val abilityId: String,
    ) : AbilityEvent

    data class RequestAbilityCreation(
        val abilityDto: Ability.Dto,
    ) : AbilityEvent

    object RequestInitialiseAbilities : AbilityEvent
}
