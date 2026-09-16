package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.AbilityExecution
import com.mkz.rpg.effect.domain.Effect.EffectTarget

class ReceiveAbilityEffects(
    private val searchAbilityById: SearchAbilityById,
    private val battleUnitRepository: BattleUnitRepository,
    private val abilityExecution: AbilityExecution,
    private val applyEffect: ApplyEffect,
    private val applyOnDefeatedEffectsToNearbyAllies: ApplyOnDefeatedEffectsToNearbyAllies,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
        row: Int,
        column: Int,
    ) {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return
        val ability = searchAbilityById(abilityId) ?: throw AbilityDoesNotExists()
        val applications = abilityExecution(casterId = battleUnitId, ability = ability, selectedRow = row, selectedColumn = column)
        applications.forEach { application -> applyEffect(application) }
        applications
            .map { application -> application.target }
            .filterIsInstance<EffectTarget.Unit>()
            .distinctBy { unitTarget -> unitTarget.id }
            .forEach { unitTarget ->
                val target = battleUnitRepository.searchById(unitTarget.id)
                if (target?.isDefeated() == true) {
                    applyOnDefeatedEffectsToNearbyAllies(battleUnitId = unitTarget.id)
                }
            }
    }
}
