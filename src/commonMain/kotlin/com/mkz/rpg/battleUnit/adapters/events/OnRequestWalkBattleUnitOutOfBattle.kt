package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.WalkBattleUnitOutOfBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestWalkBattleUnitOutOfBattle(
    private val walkBattleUnitOutOfBattle: WalkBattleUnitOutOfBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestWalkBattleUnitOutOfBattle> { event ->
            walkBattleUnitOutOfBattle(
                battleUnitId = event.battleUnitId,
                walkToRow = event.walkToRow,
                walkToColumn = event.walkToColumn,
            )
        }
}
