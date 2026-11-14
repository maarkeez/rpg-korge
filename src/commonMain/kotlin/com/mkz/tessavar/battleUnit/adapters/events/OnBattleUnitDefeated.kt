package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.RequestLeaveBattle
import com.mkz.tessavar.battleUnit.usecases.commands.TriggerOnDefeatedEffects
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleUnitDefeated(
    triggerOnDefeatedEffects: TriggerOnDefeatedEffects,
    requestLeaveBattle: RequestLeaveBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
            requestLeaveBattle(battleUnitId = event.battleUnitId)
            triggerOnDefeatedEffects(
                battleUnitId = event.battleUnitId,
                defeatedAtRow = event.defeatedAtRow,
                defeatedAtColumn = event.defeatedAtColumn,
            )
        }
}
