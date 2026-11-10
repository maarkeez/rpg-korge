package com.mkz.rpg.effect.usecases.services

import com.mkz.rpg.effect.domain.Effect

class FakeEffectLoader(
    private val effects: List<Effect.Dto> = emptyList(),
) : EffectLoader {
    override fun loadEffects(): List<Effect.Dto> = effects
}
