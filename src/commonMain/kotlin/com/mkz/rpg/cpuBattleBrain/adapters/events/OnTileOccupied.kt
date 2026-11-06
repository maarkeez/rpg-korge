package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ResolveCpuBattleUnitWalk
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnTileOccupied(
    resolveCpuBattleUnitWalk: ResolveCpuBattleUnitWalk,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.TileOccupied> { event ->
            resolveCpuBattleUnitWalk(battleUnitId = event.occupantId, row = event.row, column = event.column)
        }
}
