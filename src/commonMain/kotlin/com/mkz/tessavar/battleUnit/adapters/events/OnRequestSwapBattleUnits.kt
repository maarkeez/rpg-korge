package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.SwapBattleUnits
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestSwapBattleUnits(
    private val swapBattleUnits: SwapBattleUnits,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestSwapBattleUnits> { event ->
            swapBattleUnits(battleUnitId = event.battleUnitId, otherBattleUnitId = event.otherBattleUnitId)
        }
}
