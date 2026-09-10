package com.mkz.rpg.effect.adapters.storage

import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.EffectRepository

class InMemoryEffectRepository : EffectRepository {
    private val effects = mutableMapOf<String, Effect>()

    override fun create(effect: Effect) {
        effects[effect.toDto().id] = effect
    }

    override fun searchById(id: String) = effects[id]
}
