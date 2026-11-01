package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.usecases.commands.ProgressReachTasks
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitMoved(
    progressReachTasks: ProgressReachTasks,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitMoved> { event ->
            // A teleport's path is only its destination.
            progressReachTasks(battleUnitId = event.battleUnitId, path = event.path.map { position -> PositionDto(row = position.row, column = position.column) })
        }
}
