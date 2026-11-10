package com.mkz.rpg.ability.usecases.services

import com.mkz.rpg.ability.domain.Ability

class FakeAbilityLoader(
    private val abilities: List<Ability.Dto> = emptyList(),
) : AbilityLoader {
    override fun loadAbilities(): List<Ability.Dto> = abilities
}
