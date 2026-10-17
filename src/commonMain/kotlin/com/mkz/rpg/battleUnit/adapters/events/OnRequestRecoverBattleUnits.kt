package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.RecoverBattleUnits
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestRecoverBattleUnits(
    recoverBattleUnits: RecoverBattleUnits,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestRecoverBattleUnits> { event ->
            recoverBattleUnits(battleUnitIds = event.battleUnitIds, percentage = event.percentage)
        }
}
