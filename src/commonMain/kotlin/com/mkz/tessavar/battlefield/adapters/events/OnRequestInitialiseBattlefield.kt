package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.usecases.commands.InitialiseBattlefield
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestInitialiseBattlefield(
    initialiseBattlefield: InitialiseBattlefield,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.RequestInitialiseBattlefield> {
            initialiseBattlefield()
        }
}
