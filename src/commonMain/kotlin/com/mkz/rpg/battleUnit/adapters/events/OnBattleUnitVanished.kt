package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.RequestBattleLeave
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

/** A vanished summon leaves the battle, and its player is defeated when it was its last battle unit. */
class OnBattleUnitVanished(
    requestBattleLeave: RequestBattleLeave,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitVanished> { event ->
            requestBattleLeave(playerId = event.playerId, battleUnitId = event.battleUnitId)
        }
}
