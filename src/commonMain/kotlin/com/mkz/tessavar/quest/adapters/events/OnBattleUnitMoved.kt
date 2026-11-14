package com.mkz.tessavar.quest.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.tessavar.quest.domain.Quest.Dto.PositionDto
import com.mkz.tessavar.quest.usecases.commands.ProgressVisitTasks
import com.mkz.tessavar.quest.usecases.commands.StartMainQuest
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

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
