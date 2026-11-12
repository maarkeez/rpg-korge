package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.usecases.commands.RecordBattleStarted

class OnBattleStarted(
    recordBattleStarted: RecordBattleStarted,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleStarted> {
            recordBattleStarted()
        }
}
