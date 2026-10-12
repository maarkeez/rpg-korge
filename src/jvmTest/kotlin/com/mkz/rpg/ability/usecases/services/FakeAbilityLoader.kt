package com.mkz.rpg.ability.usecases.services

import com.mkz.rpg.ability.domain.Ability

// Loads the given abilities, instead of reading them from the resources.
class FakeAbilityLoader(
    private val abilities: List<Ability.Dto> = emptyList(),
) : AbilityLoader {
    override fun loadAbilities(): List<Ability.Dto> = abilities
}
