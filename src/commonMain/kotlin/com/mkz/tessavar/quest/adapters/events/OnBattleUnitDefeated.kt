package com.mkz.tessavar.quest.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.tessavar.quest.usecases.commands.ProgressDefeatTasks
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleUnitDefeated(
    progressDefeatTasks: ProgressDefeatTasks,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitDefeated> { event ->
            progressDefeatTasks(playerId = event.playerId, unitId = event.unitId)
        }
}
