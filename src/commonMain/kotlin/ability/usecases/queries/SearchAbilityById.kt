package ability.usecases.queries

import ability.domain.Ability
import ability.domain.AbilityRepository

class SearchAbilityById(
    private val abilityRepository: AbilityRepository,
) {
    operator fun invoke(id: String): Ability.Dto? = abilityRepository.searchById(id)?.toDto()
}
