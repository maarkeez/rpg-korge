package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.ResolveCpuBattleUnitWalk
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnTileOccupied(
    resolveCpuBattleUnitWalk: ResolveCpuBattleUnitWalk,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.TileOccupied> { event ->
            resolveCpuBattleUnitWalk(battleUnitId = event.occupantId, row = event.row, column = event.column)
        }
}
