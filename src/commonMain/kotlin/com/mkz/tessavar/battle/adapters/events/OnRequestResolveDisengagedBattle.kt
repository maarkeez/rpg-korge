package com.mkz.tessavar.battle.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battle.usecases.commands.ResolveDisengagedBattle
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestResolveDisengagedBattle(
    resolveDisengagedBattle: ResolveDisengagedBattle,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.RequestResolveDisengagedBattle> { event ->
            resolveDisengagedBattle(joiningPlayerId = event.joiningPlayerId, joiningBattleUnitIds = event.joiningBattleUnitIds)
        }
}
