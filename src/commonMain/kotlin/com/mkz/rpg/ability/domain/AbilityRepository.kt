package com.mkz.rpg.ability.domain

interface AbilityRepository {
    fun create(ability: Ability)

    fun searchById(id: String): Ability?
}
