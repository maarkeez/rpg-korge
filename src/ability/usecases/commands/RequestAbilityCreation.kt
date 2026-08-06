package ability.usecases.commands

import ability.domain.Ability
import ability.domain.AbilityError
import ability.domain.AbilityError.AbilityEffectDoesNotExist
import ability.domain.AbilityPublisher
import ability.domain.AbilityRepository
import effect.domain.Effect
import effect.domain.EffectPublisher
import effect.domain.EffectRepository
import effect.usecases.queries.SearchEffectById
import shared.domain.EventBus

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
