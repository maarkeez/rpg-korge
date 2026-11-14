package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.usecases.commands.VanishSummons
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleFinished(
    vanishSummons: VanishSummons,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.BattleFinished> {
            vanishSummons()
        }
}
