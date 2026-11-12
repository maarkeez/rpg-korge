package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent.BattleFinished
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.usecases.commands.RecordBattleFinished

class OnBattleFinished(
    recordBattleFinished: RecordBattleFinished,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleFinished> {
            recordBattleFinished()
        }
}
