package com.mkz.tessavar.lootDrop.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.tessavar.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.tessavar.lootDrop.usecases.commands.FindDroppedItems
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleUnitMoved(
    findDroppedItems: FindDroppedItems,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitMoved> { event ->
            findDroppedItems(battleUnitId = event.battleUnitId, path = event.path.map { position -> PositionDto(row = position.row, column = position.column) })
        }
}
