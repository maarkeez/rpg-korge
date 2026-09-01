package battleunit.usecases.commands

import ability.usecases.queries.SearchAbilityById
import battleunit.domain.*
import battleunit.domain.BattleUnitError.AbilityDoesNotExists
import battleunit.domain.BattleUnitError.BattleUnitCanNotCastAbility
import battleunit.domain.BattleUnitError.InvalidCastPosition
import battleunit.usecases.queries.*
import battleunit.usecases.queries.WhereCanCast.PositionDto
import shared.domain.*

class CastAbility(
    private val whereCanCast: WhereCanCast,
    private val searchAbilityById: SearchAbilityById,
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
        row: Int,
        column: Int,
    ) {
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId) ?: return
        if(!storedBattleUnit.canCastAbility(abilityId)) throw BattleUnitCanNotCastAbility()
        val whereCanCast = whereCanCast(battleUnitId, abilityId)
        if(!whereCanCast.contains(PositionDto(row, column))) throw InvalidCastPosition()
        val ability = searchAbilityById(abilityId) ?: throw AbilityDoesNotExists()
        val (events, battleUnit) = storedBattleUnit
            .castAbility(abilityId = abilityId, abilityCooldown = ability.cooldown, abilityCost= ability.cost, row = row, column = column)
            .pullEvents()
        battleUnitRepository.update(battleUnit)
        eventBus.publish(events)
    }
}
