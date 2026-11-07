package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.rpg.lootDrop.usecases.commands.RollLootDrop
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

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
