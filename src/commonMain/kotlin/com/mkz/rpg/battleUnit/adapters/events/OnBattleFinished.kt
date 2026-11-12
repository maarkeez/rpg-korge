package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.commands.VanishSummons
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleFinished(
    vanishSummons: VanishSummons,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.BattleFinished> {
            vanishSummons()
        }
}
