package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// The CPU brain updates the engagement once a round finishes, so the next round starts with the battle units joining
// and without those leaving, or the battle finishes.
class OnEngagementUpdated(
    eventBus: EventBus,
    startNextRound: StartNextRound,
) {
    private val subscription =
        eventBus.subscribe<CpuBrainEvent.EngagementUpdated> { event ->
            startNextRound(joiningBattleUnitIds = event.joiningBattleUnitIds, leavingBattleUnitIds = event.leavingBattleUnitIds)
        }
}
