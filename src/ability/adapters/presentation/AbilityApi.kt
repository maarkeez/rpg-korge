package ability.adapters.presentation

import ability.adapters.storage.InMemoryAbilityRepository
import ability.domain.AbilityRepository
import ability.usecases.commands.RequestAbilityCreation
import effect.adapters.presentation.EffectApi
import shared.domain.EventBus

class AbilityApi(
    effectApi: EffectApi,
    eventBus: EventBus,
) {
    // Storage
    private val abilityRepository: AbilityRepository = InMemoryAbilityRepository()

    // Commands
    val requestAbilityCreation = RequestAbilityCreation(abilityRepository, effectApi.searchEffectById, eventBus)
}
