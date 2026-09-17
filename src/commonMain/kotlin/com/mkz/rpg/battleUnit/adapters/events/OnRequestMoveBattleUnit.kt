package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.MoveBattleUnit
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestMoveBattleUnit(
    private val moveBattleUnit: MoveBattleUnit,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestMoveBattleUnit> { event ->
            moveBattleUnit(
                battleUnitId = event.battleUnitId,
                moveToRow = event.moveToRow,
                moveToColumn = event.moveToColumn,
            )
        }
}
