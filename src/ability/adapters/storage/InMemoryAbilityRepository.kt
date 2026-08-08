package ability.adapters.storage

import ability.domain.Ability
import ability.domain.AbilityRepository

class InMemoryAbilityRepository: AbilityRepository {
    private val abilities = mutableMapOf<String, Ability>()

    override fun create(ability: Ability) {
        abilities[ability.toDto().id] = ability
    }

    override fun searchById(id: String) = abilities[id]
}
