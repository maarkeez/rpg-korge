package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitError.BattleUnitCanNotCastAbility
import com.mkz.rpg.battleUnit.domain.BattleUnitError.InvalidCastPosition
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast.PositionDto
import com.mkz.rpg.shared.domain.EventBus

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
