package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.WalkBattleUnitInBattle
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestWalkBattleUnitInBattle(
    private val walkBattleUnitInBattle: WalkBattleUnitInBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestWalkBattleUnitInBattle> { event ->
            walkBattleUnitInBattle(
                battleUnitId = event.battleUnitId,
                walkToRow = event.walkToRow,
                walkToColumn = event.walkToColumn,
            )
        }
}
