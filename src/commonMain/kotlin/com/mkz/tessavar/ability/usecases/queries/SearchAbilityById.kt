package com.mkz.tessavar.ability.usecases.queries

import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.domain.AbilityRepository

class SearchAbilityById(
    private val abilityRepository: AbilityRepository,
) {
    operator fun invoke(id: String): Ability.Dto? = abilityRepository.searchById(id)?.toDto()
}
