package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.wandering.usecases.commands.AwaitRespawn

class OnBattleUnitDefeated(
    awaitRespawn: AwaitRespawn,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
            awaitRespawn(battleUnitId = event.battleUnitId)
        }
}
