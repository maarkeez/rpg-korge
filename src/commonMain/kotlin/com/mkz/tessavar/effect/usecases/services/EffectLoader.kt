package com.mkz.tessavar.effect.usecases.services

import com.mkz.tessavar.effect.domain.Effect

interface EffectLoader {
    suspend fun initResources() {}

    fun loadEffects(): List<Effect.Dto>
}
