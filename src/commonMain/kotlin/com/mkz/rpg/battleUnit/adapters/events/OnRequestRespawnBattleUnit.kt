package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.RespawnBattleUnit
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestRespawnBattleUnit(
    respawnBattleUnit: RespawnBattleUnit,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestRespawnBattleUnit> { event ->
            respawnBattleUnit(battleUnitId = event.battleUnitId, row = event.row, column = event.column)
        }
}
