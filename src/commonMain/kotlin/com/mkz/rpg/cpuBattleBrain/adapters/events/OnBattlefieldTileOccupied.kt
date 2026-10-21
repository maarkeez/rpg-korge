package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ResolveCpuBattleUnitMove
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattlefieldTileOccupied(
    resolveCpuBattleUnitMove: ResolveCpuBattleUnitMove,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.BattlefieldTileOccupied> { event ->
            resolveCpuBattleUnitMove(battleUnitId = event.occupantId, row = event.row, column = event.column)
        }
}
