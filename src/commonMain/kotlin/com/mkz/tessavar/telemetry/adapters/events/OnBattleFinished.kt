package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent.BattleFinished
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.usecases.commands.RecordBattleFinished

class OnBattleFinished(
    recordBattleFinished: RecordBattleFinished,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleFinished> {
            recordBattleFinished()
        }
}
