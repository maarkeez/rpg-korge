package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.shared.domain.EventBus

/** Requests the battle unit to join the battle, when there is one, e.g.: when it is deployed during the battle. */
class RequestBattleJoin(
    private val eventBus: EventBus,
) {
    operator fun invoke(
        playerId: String,
        battleUnitId: String,
    ) {
        eventBus.publish(BattleEvent.RequestJoinBattle(battleUnitId = battleUnitId, playerId = playerId))
    }
}
