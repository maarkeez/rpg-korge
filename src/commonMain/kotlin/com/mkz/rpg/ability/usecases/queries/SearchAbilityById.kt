package com.mkz.rpg.ability.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityRepository

class SearchAbilityById(
    private val abilityRepository: AbilityRepository,
) {
    operator fun invoke(id: String): Ability.Dto? = abilityRepository.searchById(id)?.toDto()
}
