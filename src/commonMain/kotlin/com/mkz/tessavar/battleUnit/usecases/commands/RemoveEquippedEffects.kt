package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitError.BattleUnitDoesNotExists
import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.shared.domain.EventBus

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
