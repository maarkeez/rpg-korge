package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.party.usecases.commands.MarkBattleUnitMoved
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitMoved(
    markBattleUnitMoved: MarkBattleUnitMoved,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitMoved> { event ->
            markBattleUnitMoved(event.battleUnitId)
        }
}
