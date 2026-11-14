package com.mkz.tessavar.ability.adapters.storage

import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.domain.AbilityRepository

class InMemoryAbilityRepository : AbilityRepository {
    private val abilities = mutableMapOf<String, Ability>()

    override fun create(ability: Ability) {
        abilities[ability.toDto().id] = ability
    }

    override fun searchById(id: String) = abilities[id]
}
