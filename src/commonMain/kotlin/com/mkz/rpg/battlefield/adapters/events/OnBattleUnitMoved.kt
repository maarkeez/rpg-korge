package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitMoved(
    updateBattlefieldOccupancy: UpdateBattlefieldOccupancy,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitMoved> { event ->
            updateBattlefieldOccupancy(
                row = event.toRow,
                column = event.toColumn,
                battleUnitId = event.battleUnitId,
            )
        }
}
