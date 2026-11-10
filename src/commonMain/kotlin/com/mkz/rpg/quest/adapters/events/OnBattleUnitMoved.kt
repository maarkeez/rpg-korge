package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.usecases.commands.ProgressVisitTasks
import com.mkz.rpg.quest.usecases.commands.StartMainQuest
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitMoved(
    startMainQuest: StartMainQuest,
    progressVisitTasks: ProgressVisitTasks,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitMoved> { event ->
            val path = event.path.map { position -> PositionDto(row = position.row, column = position.column) }
            startMainQuest(battleUnitId = event.battleUnitId, path = path)
            progressVisitTasks(battleUnitId = event.battleUnitId, path = path)
        }
}
