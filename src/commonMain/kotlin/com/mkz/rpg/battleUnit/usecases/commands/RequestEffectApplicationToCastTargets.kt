package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitError.BattleUnitDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.AbilityExecution
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.shared.domain.EventBus

class RequestEffectApplicationToCastTargets(
    private val searchAbilityById: SearchAbilityById,
    private val battleUnitRepository: BattleUnitRepository,
    private val abilityExecution: AbilityExecution,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
        castGroup: List<Battlefield.Dto.PositionDto>,
    ) {
        battleUnitRepository.searchById(battleUnitId) ?: throw BattleUnitDoesNotExists()
        val ability = searchAbilityById(abilityId) ?: throw AbilityDoesNotExists()
        castGroup.forEach { position ->
            val applications = abilityExecution(casterId = battleUnitId, ability = ability, selectedRow = position.row, selectedColumn = position.column)
            applications.forEach { application ->
                eventBus.publish(BattleUnitEvent.RequestApplyEffect(application = application))
            }
        }
        // Published after the effect requests, so it is handled once all of them have been applied.
        eventBus.publish(BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = abilityId))
    }
}
