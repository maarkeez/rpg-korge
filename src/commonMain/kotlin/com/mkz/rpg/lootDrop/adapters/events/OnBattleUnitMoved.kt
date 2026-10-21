package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.usecases.commands.FindLoot
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitMoved(
    findLoot: FindLoot,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitMoved> { event ->
            // A teleport's path is only its destination.
            findLoot(battleUnitId = event.battleUnitId, path = event.path.map { position -> PositionDto(row = position.row, column = position.column) })
        }
}
