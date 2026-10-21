package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// The next round is requested once a round finishes (e.g.: by the CPU battle brain, once it updated the engagement), so it
// starts with the battle units joining and without those leaving, or the battle finishes.
class OnRequestStartNextRound(
    eventBus: EventBus,
    startNextRound: StartNextRound,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.RequestStartNextRound> { event ->
            startNextRound(joiningPlayerId = event.joiningPlayerId, joiningBattleUnitIds = event.joiningBattleUnitIds, leavingBattleUnitIds = event.leavingBattleUnitIds)
        }
}
