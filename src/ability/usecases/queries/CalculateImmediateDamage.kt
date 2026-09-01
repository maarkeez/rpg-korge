package ability.usecases.queries

import ability.domain.*
import effect.usecases.queries.*

class CalculateImmediateDamage(
    private val abilityRepository: AbilityRepository,
    private val searchEffectById: SearchEffectById,
) {
    operator fun invoke(id: String): Int {
        val ability = abilityRepository.searchById(id)?.toDto() ?: return 0
        return ability.effects.map { effectId -> searchEffectById(effectId)!! }
            .filter{ it.application.type == "IMMEDIATELY" }
            .sumOf { effect -> effect.power }
    }
}
