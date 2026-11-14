package com.mkz.tessavar.effect.adapters.storage

import com.mkz.tessavar.effect.domain.Effect
import com.mkz.tessavar.effect.domain.EffectRepository

class InMemoryEffectRepository : EffectRepository {
    private val effects = mutableMapOf<String, Effect>()

    override fun create(effect: Effect) {
        effects[effect.toDto().id] = effect
    }

    override fun searchById(id: String) = effects[id]
}
