package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.exploration.usecases.commands.ResumeExploration
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleFinished(
    resumeExploration: ResumeExploration,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.BattleFinished> {
            resumeExploration()
        }
}
