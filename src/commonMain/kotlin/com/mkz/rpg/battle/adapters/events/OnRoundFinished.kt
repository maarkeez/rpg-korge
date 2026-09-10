package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent.BattleRoundFinished
import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRoundFinished(
    eventBus: EventBus,
    startNextRound: StartNextRound,
) {
    private val subscription =
        eventBus.subscribe<BattleRoundFinished> {
            startNextRound()
        }
}
