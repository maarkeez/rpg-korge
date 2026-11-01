package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.rpg.quest.usecases.commands.ProgressDefeatTasks
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDefeated(
    progressDefeatTasks: ProgressDefeatTasks,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitDefeated> { event ->
            progressDefeatTasks(playerId = event.playerId, unitId = event.unitId)
        }
}
