package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.RecoverBattleUnits
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestRecoverBattleUnits(
    recoverBattleUnits: RecoverBattleUnits,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestRecoverBattleUnits> { event ->
            recoverBattleUnits(battleUnitIds = event.battleUnitIds, percentage = event.percentage)
        }
}
