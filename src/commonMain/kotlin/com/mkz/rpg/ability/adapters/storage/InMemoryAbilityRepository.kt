package com.mkz.rpg.ability.adapters.storage

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityRepository

class InMemoryAbilityRepository : AbilityRepository {
    private val abilities = mutableMapOf<String, Ability>()

    override fun create(ability: Ability) {
        abilities[ability.toDto().id] = ability
    }

    override fun searchById(id: String) = abilities[id]
}
