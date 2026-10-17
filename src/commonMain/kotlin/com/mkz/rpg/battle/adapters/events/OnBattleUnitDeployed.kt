package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.usecases.commands.JoinBattle
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDeployed(
    eventBus: EventBus,
    joinBattle: JoinBattle,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
            joinBattle(event.battleUnitId)
        }
}
