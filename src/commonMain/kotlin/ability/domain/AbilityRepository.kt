package ability.domain

interface AbilityRepository {
    fun create(ability: Ability)
    fun searchById(id: String): Ability?
}
