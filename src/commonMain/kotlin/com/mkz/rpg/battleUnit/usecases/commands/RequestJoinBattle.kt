package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.shared.domain.EventBus

class RequestJoinBattle(
    private val eventBus: EventBus,
) {
    operator fun invoke(
        playerId: String,
        battleUnitId: String,
    ) {
        eventBus.publish(BattleEvent.RequestJoinBattle(battleUnitId = battleUnitId, playerId = playerId))
    }
}
