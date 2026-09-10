package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDeployed(
    updateBattlefieldOccupancy: UpdateBattlefieldOccupancy,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitDeployed> { event ->
            updateBattlefieldOccupancy(
                row = event.row,
                column = event.column,
                battleUnitId = event.battleUnitId,
            )
        }
}
