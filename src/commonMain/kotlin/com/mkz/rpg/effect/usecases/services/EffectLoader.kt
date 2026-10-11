package com.mkz.rpg.effect.usecases.services

import com.mkz.rpg.effect.domain.Effect

interface EffectLoader {
    suspend fun initResources() {}

    fun loadEffects(): List<Effect.Dto>
}
