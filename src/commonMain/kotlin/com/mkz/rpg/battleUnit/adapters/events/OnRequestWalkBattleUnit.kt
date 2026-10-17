package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.WalkBattleUnit
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestWalkBattleUnit(
    private val walkBattleUnit: WalkBattleUnit,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestWalkBattleUnit> { event ->
            walkBattleUnit(
                battleUnitId = event.battleUnitId,
                walkToRow = event.walkToRow,
                walkToColumn = event.walkToColumn,
            )
        }
}
