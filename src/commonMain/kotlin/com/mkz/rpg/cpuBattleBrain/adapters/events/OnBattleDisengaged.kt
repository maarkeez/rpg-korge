package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.JoinDisengagedBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// The CPU battle units within reach join the battle as soon as it is disengaged during a round, so it does not wait for the round to finish.
class OnBattleDisengaged(
    joinDisengagedBattle: JoinDisengagedBattle,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.BattleDisengaged> { event ->
            joinDisengagedBattle(battleUnitIds = event.battleUnitIds)
        }
}
