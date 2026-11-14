package com.mkz.tessavar.ability.usecases.services

import com.mkz.tessavar.ability.domain.Ability

class FakeAbilityLoader(
    private val abilities: List<Ability.Dto> = emptyList(),
) : AbilityLoader {
    override fun loadAbilities(): List<Ability.Dto> = abilities
}
