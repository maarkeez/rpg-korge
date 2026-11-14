package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.ability.usecases.queries.SearchAbilityById
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.BattleUnitCanNotCastAbility
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.BattleUnitDoesNotExists
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.InvalidCastPosition
import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battleUnit.usecases.queries.WhereCanCast
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.shared.domain.EventBus

class CastAbility(
    private val whereCanCast: WhereCanCast,
    private val searchAbilityById: SearchAbilityById,
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
        castGroup: WhereCanCast.CastGroup,
    ) {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: throw BattleUnitDoesNotExists()
        val ability = searchAbilityById(abilityId) ?: throw AbilityDoesNotExists()
        if (!battleUnit.canCastAbility(ability)) throw BattleUnitCanNotCastAbility()
        val castGroupsWhereCanCast = whereCanCast(battleUnitId, abilityId)
        if (!castGroupsWhereCanCast.contains(castGroup)) throw InvalidCastPosition()
        val (events, updatedBattleUnit) =
            battleUnit
                .castAbility(
                    abilityId = abilityId,
                    abilityCooldown = ability.cooldown,
                    abilityManaCost = ability.manaCost,
                    castGroup = castGroup.positions.map { position -> Battlefield.Dto.PositionDto(row = position.row, column = position.column) },
                ).pullEvents()
        battleUnitRepository.update(updatedBattleUnit)
        eventBus.publish(events)
    }
}
