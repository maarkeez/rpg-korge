package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.shared.domain.EventBus

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
