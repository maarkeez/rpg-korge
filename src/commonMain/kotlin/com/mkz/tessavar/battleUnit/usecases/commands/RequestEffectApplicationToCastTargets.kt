package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.ability.usecases.queries.SearchAbilityById
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.BattleUnitDoesNotExists
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battleUnit.usecases.services.CastResolution
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.shared.domain.EventBus

class RequestEffectApplicationToCastTargets(
    private val searchAbilityById: SearchAbilityById,
    private val battleUnitRepository: BattleUnitRepository,
    private val castResolution: CastResolution,
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
            val applications = castResolution(casterId = battleUnitId, ability = ability, targetedRow = position.row, targetedColumn = position.column)
            applications.forEach { application ->
                eventBus.publish(BattleUnitEvent.RequestApplyEffect(application = application))
            }
        }
        eventBus.publish(BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = abilityId))
    }
}
