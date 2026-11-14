package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.RespawnBattleUnit
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestRespawnBattleUnit(
    respawnBattleUnit: RespawnBattleUnit,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestRespawnBattleUnit> { event ->
            respawnBattleUnit(battleUnitId = event.battleUnitId, row = event.row, column = event.column)
        }
}
