package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.RequestEngageEnemies
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.EngageEnemies
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestEngageEnemies(
    engageEnemies: EngageEnemies,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<RequestEngageEnemies> { event ->
            engageEnemies(exploringPlayerId = event.exploringPlayerId)
        }
}
