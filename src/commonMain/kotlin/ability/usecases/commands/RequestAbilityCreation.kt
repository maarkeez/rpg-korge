package ability.usecases.commands

import ability.domain.*
import ability.domain.AbilityError.AbilityEffectDoesNotExist
import effect.usecases.queries.*
import shared.domain.*

class RequestAbilityCreation(
    private val abilityRepository: AbilityRepository,
    private val searchEffectById: SearchEffectById,
    private val eventBus: EventBus,
) {
    operator fun invoke(abilityDto: Ability.Dto) {
        if(abilityRepository.searchById(abilityDto.id) != null) return
        val allEffectsExist = abilityDto.effects.all { effectId-> searchEffectById(effectId) != null }
        if(!allEffectsExist) throw AbilityEffectDoesNotExist()
        val (events, ability) = Ability.create(abilityDto).pullEvents()
        abilityRepository.create(ability)
        eventBus.publish(events)
    }
}
