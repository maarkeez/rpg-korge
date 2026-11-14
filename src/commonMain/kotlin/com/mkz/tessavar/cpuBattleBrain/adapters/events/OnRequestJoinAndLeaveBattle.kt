package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.RequestJoinAndLeaveBattle
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.JoinAndLeaveBattle
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestJoinAndLeaveBattle(
    joinAndLeaveBattle: JoinAndLeaveBattle,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<RequestJoinAndLeaveBattle> { event ->
            joinAndLeaveBattle(battleUnitIds = event.battleUnitIds)
        }
}
