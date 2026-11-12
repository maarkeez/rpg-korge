package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.RequestLeaveBattle
import com.mkz.rpg.battleUnit.usecases.commands.TriggerOnDefeatedEffects
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

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
