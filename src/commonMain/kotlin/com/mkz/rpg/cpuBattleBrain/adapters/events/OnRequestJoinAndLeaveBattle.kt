package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.RequestJoinAndLeaveBattle
import com.mkz.rpg.cpuBattleBrain.usecases.commands.JoinAndLeaveBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// The CPU battle units join and leave the battle once the CPU battle units out of the battle have wandered, after the round finished.
class OnRequestJoinAndLeaveBattle(
    joinAndLeaveBattle: JoinAndLeaveBattle,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<RequestJoinAndLeaveBattle> { event ->
            joinAndLeaveBattle(battleUnitIds = event.battleUnitIds)
        }
}
