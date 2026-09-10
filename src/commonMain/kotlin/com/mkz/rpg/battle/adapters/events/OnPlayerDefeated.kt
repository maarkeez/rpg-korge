package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.FinishBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnPlayerDefeated(
    eventBus: EventBus,
    finishBattle: FinishBattle,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.PlayerDefeated> { event ->
            finishBattle()
        }
}
