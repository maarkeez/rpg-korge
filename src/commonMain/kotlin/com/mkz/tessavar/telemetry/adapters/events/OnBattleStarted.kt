package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent.BattleStarted
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.usecases.commands.RecordBattleStarted

class OnBattleStarted(
    recordBattleStarted: RecordBattleStarted,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleStarted> {
            recordBattleStarted()
        }
}
