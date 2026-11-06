package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.ResolveDisengagedBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// Requested once the battle is disengaged during a round (e.g.: by the CPU battle brain, once the CPU battle units within reach joined the disengaged battle),
// so it goes on with the battle units joining, or finishes.
class OnRequestResolveDisengagedBattle(
    resolveDisengagedBattle: ResolveDisengagedBattle,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.RequestResolveDisengagedBattle> { event ->
            resolveDisengagedBattle(joiningPlayerId = event.joiningPlayerId, joiningBattleUnitIds = event.joiningBattleUnitIds)
        }
}
