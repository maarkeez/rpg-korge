package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.party.usecases.commands.LeaveParty
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDefeated(
    leaveParty: LeaveParty,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
            leaveParty(event.battleUnitId)
        }
}
