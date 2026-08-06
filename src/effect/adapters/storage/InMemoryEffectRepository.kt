package effect.adapters.storage

import effect.domain.Effect
import effect.domain.EffectRepository
import player.domain.Player
import kotlin.collections.set

class InMemoryEffectRepository : EffectRepository {
    private val effects = mutableMapOf<String, Effect>()

    override fun create(effect: Effect) {
        effects[effect.toDto().id] = effect
    }

    override fun searchById(id: String) = effects[id]
}
