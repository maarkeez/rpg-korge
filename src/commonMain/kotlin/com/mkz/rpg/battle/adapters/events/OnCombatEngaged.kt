package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.usecases.commands.InitialiseBattle
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnCombatEngaged(
    initialiseBattle: InitialiseBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<CpuBrainEvent.CombatEngaged> {
            initialiseBattle()
        }
}
