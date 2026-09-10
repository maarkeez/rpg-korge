package battleUnit.usecases.commands

import ability.usecases.queries.SearchAbilityById
import battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import battleUnit.domain.BattleUnitError.BattleUnitCanNotCastAbility
import battleUnit.domain.BattleUnitError.InvalidCastPosition
import battleUnit.domain.BattleUnitRepository
import battleUnit.usecases.queries.WhereCanCast
import battleUnit.usecases.queries.WhereCanCast.PositionDto
import shared.domain.EventBus

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
        val ability = searchAbilityById(abilityId) ?: throw AbilityDoesNotExists()
        if (!storedBattleUnit.canCastAbility(ability)) throw BattleUnitCanNotCastAbility()
        val whereCanCast = whereCanCast(battleUnitId, abilityId)
        if (!whereCanCast.contains(PositionDto(row, column))) throw InvalidCastPosition()
        val (events, battleUnit) =
            storedBattleUnit
                .castAbility(abilityId = abilityId, abilityCooldown = ability.cooldown, abilityCost = ability.cost, row = row, column = column)
                .pullEvents()
        battleUnitRepository.update(battleUnit)
        eventBus.publish(events)
    }
}
