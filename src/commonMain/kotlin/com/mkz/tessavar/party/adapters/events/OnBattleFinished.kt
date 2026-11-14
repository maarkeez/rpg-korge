package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.party.usecases.commands.ResumeExploration
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleFinished(
    resumeExploration: ResumeExploration,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.BattleFinished> {
            resumeExploration()
        }
}
