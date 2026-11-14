package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.party.usecases.commands.MarkBattleUnitMoved
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleUnitMoved(
    markBattleUnitMoved: MarkBattleUnitMoved,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitMoved> { event ->
            markBattleUnitMoved(event.battleUnitId)
        }
}
