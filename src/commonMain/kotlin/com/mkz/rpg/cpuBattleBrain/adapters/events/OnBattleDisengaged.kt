package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ReengageEnemies
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// The enemies are re-engaged as soon as the battle is disengaged during a round, so it does not wait for the round to finish.
class OnBattleDisengaged(
    reengageEnemies: ReengageEnemies,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.BattleDisengaged> { event ->
            reengageEnemies(battleUnitIds = event.battleUnitIds)
        }
}
