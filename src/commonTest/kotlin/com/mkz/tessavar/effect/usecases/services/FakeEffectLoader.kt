package com.mkz.tessavar.effect.usecases.services

import com.mkz.tessavar.effect.domain.Effect

class FakeEffectLoader(
    private val effects: List<Effect.Dto> = emptyList(),
) : EffectLoader {
    override fun loadEffects(): List<Effect.Dto> = effects
}
