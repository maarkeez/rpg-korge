package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.battle.usecases.commands.LeaveBattle
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

/** A vanished summon leaves the battle, and its player is defeated when it was its last battle unit. */
class OnBattleUnitVanished(
    eventBus: EventBus,
    leaveBattle: LeaveBattle,
    defeatPlayer: DefeatPlayer,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitVanished> { event ->
            leaveBattle(event.battleUnitId)
            defeatPlayer(event.playerId)
        }
}
