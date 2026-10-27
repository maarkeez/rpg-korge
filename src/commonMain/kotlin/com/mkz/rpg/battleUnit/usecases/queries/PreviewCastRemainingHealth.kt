package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.AbilityExecution
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto
import com.mkz.rpg.effect.usecases.queries.SearchEffectById

/**
 * The health each battle unit would have left right after the ability is cast on the cast group, keyed by battle unit
 * id. Only the battle units whose health the cast changes immediately (e.g.: damaged or healed) are listed.
 */
class PreviewCastRemainingHealth(
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val abilityExecution: AbilityExecution,
) {
    companion object {
        private val HEALTH_OUTCOME_TYPES = setOf(TypeDto.DECREASE_HEALTH, TypeDto.INCREASE_HEALTH)
    }

    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
        castGroup: List<Battlefield.Dto.PositionDto>,
    ): Map<String, Int> {
        val ability = searchAbilityById(abilityId) ?: return emptyMap()
        // The effects are applied to copies of the battle units, in the order the cast applies them, without storing them.
        val previewedBattleUnits = mutableMapOf<String, BattleUnit>()
        castGroup
            .flatMap { position -> abilityExecution(casterId = battleUnitId, ability = ability, selectedRow = position.row, selectedColumn = position.column) }
            .forEach { application ->
                val targetId = application.target.battleUnitId ?: return@forEach
                val effect = searchEffectById(application.effectId) ?: return@forEach
                if (effect.application.type != ApplicationTypeDto.IMMEDIATELY || effect.outcome.type !in HEALTH_OUTCOME_TYPES) return@forEach
                val battleUnit = previewedBattleUnits[targetId] ?: battleUnitRepository.searchById(targetId) ?: return@forEach
                val position = searchPosition(targetId) ?: return@forEach
                previewedBattleUnits[targetId] = battleUnit.applyImmediateEffect(effect = effect, currentRow = position.row, currentColumn = position.column)
            }
        return previewedBattleUnits.mapValues { (_, battleUnit) -> battleUnit.toDto().remainingHealthPoints }
    }
}
