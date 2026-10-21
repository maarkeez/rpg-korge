package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.queries.HasAllBattleUnitsDefeated
import com.mkz.rpg.shared.domain.EventBus

/**
 * Requests the battle unit to leave the battle, when there is one, e.g.: when it is defeated or vanishes. Then requests
 * its player to be defeated, once all their battle units are defeated, in a battle or while exploring.
 */
class RequestBattleLeave(
    private val hasAllBattleUnitsDefeated: HasAllBattleUnitsDefeated,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        playerId: String,
        battleUnitId: String,
    ) {
        eventBus.publish(BattleEvent.RequestLeaveBattle(battleUnitId = battleUnitId))
        if (hasAllBattleUnitsDefeated(playerId)) eventBus.publish(BattleEvent.RequestDefeatPlayer(playerId = playerId))
    }
}
