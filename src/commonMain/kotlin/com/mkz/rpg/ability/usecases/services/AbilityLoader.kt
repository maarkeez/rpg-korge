package com.mkz.rpg.ability.usecases.services

import com.mkz.rpg.ability.domain.Ability

interface AbilityLoader {
    suspend fun initResources() {}

    fun loadAbilities(): List<Ability.Dto>
}
