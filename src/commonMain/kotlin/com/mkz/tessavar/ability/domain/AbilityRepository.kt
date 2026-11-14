package com.mkz.tessavar.ability.domain

interface AbilityRepository {
    fun create(ability: Ability)

    fun searchById(id: String): Ability?
}
