package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitError.BattleUnitDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.shared.domain.EventBus

/** Removes the effects an unequipped item applied to its battle unit while it was equipped. */
class RemoveEquippedEffects(
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: throw BattleUnitDoesNotExists()
        val (events, updatedBattleUnit) = battleUnit.removeEquippedEffects(lootableItemId).pullEvents()
        battleUnitRepository.update(updatedBattleUnit)
        eventBus.publish(events)
    }
}
