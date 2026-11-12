package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.shared.domain.EventBus

class RequestLeaveBattle(
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitId: String) {
        eventBus.publish(BattleEvent.RequestLeaveBattle(battleUnitId = battleUnitId))
    }
}
