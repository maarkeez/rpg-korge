package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.AwaitRespawn
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleUnitDefeated(
    awaitRespawn: AwaitRespawn,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
            awaitRespawn(battleUnitId = event.battleUnitId)
        }
}
