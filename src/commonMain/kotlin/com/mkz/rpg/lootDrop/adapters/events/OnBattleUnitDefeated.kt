package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.rpg.lootDrop.usecases.commands.DropLoot
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDefeated(
    dropLoot: DropLoot,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitDefeated> { event ->
            dropLoot(
                unitId = event.unitId,
                row = event.defeatedAtRow,
                column = event.defeatedAtColumn,
            )
        }
}
