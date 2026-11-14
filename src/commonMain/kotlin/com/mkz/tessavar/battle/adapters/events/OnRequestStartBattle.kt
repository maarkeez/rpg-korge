package com.mkz.tessavar.battle.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battle.usecases.commands.StartBattle
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

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
