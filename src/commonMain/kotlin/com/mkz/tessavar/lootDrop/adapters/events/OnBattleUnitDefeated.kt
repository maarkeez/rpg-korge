package com.mkz.tessavar.lootDrop.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.tessavar.lootDrop.usecases.commands.RollLootDrop
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleUnitDefeated(
    rollLootDrop: RollLootDrop,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitDefeated> { event ->
            rollLootDrop(
                unitId = event.unitId,
                row = event.defeatedAtRow,
                column = event.defeatedAtColumn,
            )
        }
}
