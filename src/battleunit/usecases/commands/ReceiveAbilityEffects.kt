package battleunit.usecases.commands

import ability.usecases.queries.SearchAbilityById
import battlefield.usecases.queries.SearchOccupant
import battleunit.domain.*
import battleunit.domain.BattleUnitError.AbilityDoesNotExists
import battleunit.domain.BattleUnitError.FailedToReceiveAbilityEffects
import battleunit.usecases.queries.*
import effect.usecases.queries.SearchEffectById
import shared.domain.*

class ReceiveAbilityEffects(
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    private val searchOccupant: SearchOccupant,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
        row: Int,
        column: Int,
    ) {
        battleUnitRepository.searchById(battleUnitId) ?: return
        val ability = searchAbilityById(abilityId) ?: throw AbilityDoesNotExists()
        val effects = ability.effects.map { effectId -> searchEffectById(effectId) ?: throw FailedToReceiveAbilityEffects() }
        // TODO: Implement other target patterns
        if(ability.targetPattern == "ADJACENT_ENEMY"){
            val occupantId = searchOccupant(row, column) ?: throw FailedToReceiveAbilityEffects()
            val occupantBattleUnit = battleUnitRepository.searchById(occupantId) ?: throw FailedToReceiveAbilityEffects()
            // TODO: Implement all the effect logic: Probability, modifiers, applications, etc
            val (events, updatedBattleUnit) = effects.fold(occupantBattleUnit)  { battleUnit, effect ->
                battleUnit
                    .receiveImmediateEffect(effect.id)
                    .applyImmediateEffect(effect)
            }.pullEvents()
            battleUnitRepository.update(updatedBattleUnit)
            eventBus.publish(events)
        }
    }
}
