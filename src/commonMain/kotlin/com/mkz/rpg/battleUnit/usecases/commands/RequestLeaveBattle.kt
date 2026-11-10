package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.queries.AreAllBattleUnitsDefeated
import com.mkz.rpg.shared.domain.EventBus

class RequestLeaveBattle(
    private val areAllBattleUnitsDefeated: AreAllBattleUnitsDefeated,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        playerId: String,
        battleUnitId: String,
    ) {
        eventBus.publish(BattleEvent.RequestLeaveBattle(battleUnitId = battleUnitId))
        if (areAllBattleUnitsDefeated(playerId)) eventBus.publish(BattleEvent.RequestDefeatPlayer(playerId = playerId))
    }
}
