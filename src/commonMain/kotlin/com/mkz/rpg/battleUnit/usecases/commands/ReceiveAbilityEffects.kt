package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.AbilityExecution
import com.mkz.rpg.shared.domain.EventBus

class ReceiveAbilityEffects(
    private val searchAbilityById: SearchAbilityById,
    private val battleUnitRepository: BattleUnitRepository,
    private val abilityExecution: AbilityExecution,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
        row: Int,
        column: Int,
    ) {
        battleUnitRepository.searchById(battleUnitId) ?: return
        val ability = searchAbilityById(abilityId) ?: throw AbilityDoesNotExists()
        val applications = abilityExecution(casterId = battleUnitId, ability = ability, selectedRow = row, selectedColumn = column)
        applications.forEach { application ->
            eventBus.publish(BattleUnitEvent.RequestApplyEffect(application = application))
        }
    }
}
