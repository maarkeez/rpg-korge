package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.JoinBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestJoinBattle(
    joinBattle: JoinBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.RequestJoinBattle> { event ->
            joinBattle(battleUnitId = event.battleUnitId, playerId = event.playerId)
        }
}
