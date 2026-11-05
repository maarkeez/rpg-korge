package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.WalkBattleUnitInBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

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
