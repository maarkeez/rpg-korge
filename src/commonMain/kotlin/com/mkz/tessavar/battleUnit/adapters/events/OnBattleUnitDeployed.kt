package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.RequestJoinBattle
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleUnitDeployed(
    requestJoinBattle: RequestJoinBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
            requestJoinBattle(playerId = event.playerId, battleUnitId = event.battleUnitId)
        }
}
