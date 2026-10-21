package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.StartBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestStartBattle(
    startBattle: StartBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.RequestStartBattle> { event ->
            startBattle(
                engagingPlayerId = event.engagingPlayerId,
                engagedPlayerId = event.engagedPlayerId,
                engagingBattleUnitIds = event.engagingBattleUnitIds,
                engagedBattleUnitIds = event.engagedBattleUnitIds,
            )
        }
}
