package com.mkz.tessavar.ability.usecases.services

import com.mkz.tessavar.ability.domain.Ability

interface AbilityLoader {
    suspend fun initResources() {}

    fun loadAbilities(): List<Ability.Dto>
}
