package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.SwapBattleUnits
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestSwapBattleUnits(
    private val swapBattleUnits: SwapBattleUnits,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestSwapBattleUnits> { event ->
            swapBattleUnits(battleUnitId = event.battleUnitId, otherBattleUnitId = event.otherBattleUnitId)
        }
}
