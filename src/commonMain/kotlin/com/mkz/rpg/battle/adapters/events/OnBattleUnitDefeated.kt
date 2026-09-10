package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDefeated(
    eventBus: EventBus,
    defeatPlayer: DefeatPlayer,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
            defeatPlayer(event.playerId)
        }
}
