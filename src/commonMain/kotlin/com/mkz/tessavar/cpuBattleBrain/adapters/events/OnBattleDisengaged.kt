package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.JoinDisengagedBattle
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleDisengaged(
    joinDisengagedBattle: JoinDisengagedBattle,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.BattleDisengaged> { event ->
            joinDisengagedBattle(battleUnitIds = event.battleUnitIds)
        }
}
