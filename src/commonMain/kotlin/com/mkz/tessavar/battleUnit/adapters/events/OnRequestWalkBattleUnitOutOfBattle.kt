package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.WalkBattleUnitOutOfBattle
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

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
