package battleunit.usecases.commands

import ability.usecases.queries.SearchAbilityById
import battlefield.usecases.queries.SearchOccupant
import battleunit.domain.*
import battleunit.domain.BattleUnitError.AbilityDoesNotExists
import battleunit.domain.BattleUnitError.FailedToReceiveAbilityEffects
import effect.domain.Effect
import effect.usecases.queries.SearchEffectById
import shared.domain.*
import unit.usecases.queries.SearchUnitById

class ReceiveAbilityEffects(
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    private val searchOccupant: SearchOccupant,
    private val searchUnitById: SearchUnitById,
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
        val occupantId = searchOccupant(row, column) ?: throw FailedToReceiveAbilityEffects()
        if(ability.targetPattern == "ADJACENT_ENEMY") {
            val occupantBattleUnit = battleUnitRepository.searchById(occupantId) ?: throw FailedToReceiveAbilityEffects()
            val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: throw FailedToReceiveAbilityEffects()
            if(battleUnit.isSamePlayer(occupantBattleUnit)) throw FailedToReceiveAbilityEffects()
        }
        if(ability.targetPattern == "SELF") {
            if(occupantId != battleUnitId) throw FailedToReceiveAbilityEffects()
        }
        receiveAbilityEffects(battleUnitId = occupantId, effects = effects)
    }

    private fun receiveAbilityEffects(battleUnitId: String, effects: List<Effect.Dto>) {
        val occupantBattleUnit = battleUnitRepository.searchById(battleUnitId) ?: throw FailedToReceiveAbilityEffects()
        val unit = searchUnitById(occupantBattleUnit.toDto().unitId) ?: throw FailedToReceiveAbilityEffects()
        // TODO: Implement all the effect logic: Probability, modifiers, applications, etc
        val (events, updatedBattleUnit) = effects.fold(occupantBattleUnit) { battleUnit, effect ->
            battleUnit
                .receiveImmediateEffect(effect.id)
                .applyImmediateEffect(effect, unit)
        }.pullEvents()
        battleUnitRepository.update(updatedBattleUnit)
        eventBus.publish(events)
    }
}
