package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.commands.VanishSummonedBattleUnits
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleFinishedWithoutWinner(
    vanishSummonedBattleUnits: VanishSummonedBattleUnits,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.BattleFinishedWithoutWinner> {
            vanishSummonedBattleUnits()
        }
}
