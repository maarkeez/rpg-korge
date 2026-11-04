package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.ApplyOnDefeatedEffectsToAdjacentAllies
import com.mkz.rpg.battleUnit.usecases.commands.RequestBattleLeave
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDefeated(
    applyOnDefeatedEffectsToAdjacentAllies: ApplyOnDefeatedEffectsToAdjacentAllies,
    requestBattleLeave: RequestBattleLeave,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
            requestBattleLeave(playerId = event.playerId, battleUnitId = event.battleUnitId)
            applyOnDefeatedEffectsToAdjacentAllies(
                battleUnitId = event.battleUnitId,
                defeatedAtRow = event.defeatedAtRow,
                defeatedAtColumn = event.defeatedAtColumn,
            )
        }
}
