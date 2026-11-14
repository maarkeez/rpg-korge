package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.ApplyEffect
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestApplyEffect(
    private val applyEffect: ApplyEffect,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestApplyEffect> { event ->
            applyEffect(event.application)
        }
}
