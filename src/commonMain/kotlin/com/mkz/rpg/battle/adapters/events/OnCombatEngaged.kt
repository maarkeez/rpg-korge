package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.usecases.commands.StartBattle
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnCombatEngaged(
    startBattle: StartBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<CpuBrainEvent.CombatEngaged> { event ->
            startBattle(
                engagingPlayerId = event.playerId,
                engagedPlayerId = event.engagedPlayerId,
                engagingBattleUnitIds = event.battleUnitIds,
            )
        }
}
