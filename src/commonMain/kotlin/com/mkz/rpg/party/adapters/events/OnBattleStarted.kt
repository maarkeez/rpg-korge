package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.party.usecases.commands.PauseExploration
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleStarted(
    pauseExploration: PauseExploration,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.BattleStarted> {
            pauseExploration()
        }
}
