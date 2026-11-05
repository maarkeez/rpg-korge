package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.ApplyOnDefeatedEffectsToAdjacentAllies
import com.mkz.rpg.battleUnit.usecases.commands.RequestLeaveBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDefeated(
    applyOnDefeatedEffectsToAdjacentAllies: ApplyOnDefeatedEffectsToAdjacentAllies,
    requestLeaveBattle: RequestLeaveBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
            requestLeaveBattle(playerId = event.playerId, battleUnitId = event.battleUnitId)
            applyOnDefeatedEffectsToAdjacentAllies(
                battleUnitId = event.battleUnitId,
                defeatedAtRow = event.defeatedAtRow,
                defeatedAtColumn = event.defeatedAtColumn,
            )
        }
}
