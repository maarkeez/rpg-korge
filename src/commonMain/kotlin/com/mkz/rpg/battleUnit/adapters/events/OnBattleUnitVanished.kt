package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.RequestLeaveBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

/** A vanished summon leaves the battle, and its player is defeated when it was its last battle unit. */
class OnBattleUnitVanished(
    requestLeaveBattle: RequestLeaveBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitVanished> { event ->
            requestLeaveBattle(playerId = event.playerId, battleUnitId = event.battleUnitId)
        }
}
