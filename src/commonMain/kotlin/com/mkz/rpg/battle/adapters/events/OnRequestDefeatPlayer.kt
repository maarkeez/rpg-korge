package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestDefeatPlayer(
    defeatPlayer: DefeatPlayer,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.RequestDefeatPlayer> { event ->
            defeatPlayer(event.playerId)
        }
}
