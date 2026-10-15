package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.lootDrop.usecases.commands.DropLoot
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDefeated(
    dropLoot: DropLoot,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
            dropLoot(
                battleUnitId = event.battleUnitId,
                defeatedAtRow = event.defeatedAtRow,
                defeatedAtColumn = event.defeatedAtColumn,
            )
        }
}
