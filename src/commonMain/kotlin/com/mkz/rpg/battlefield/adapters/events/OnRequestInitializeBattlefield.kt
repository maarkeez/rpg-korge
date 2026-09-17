package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.usecases.commands.InitializeBattlefield
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitializeBattlefield(
    private val initializeBattlefield: InitializeBattlefield,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.RequestInitializeBattlefield> { event ->
            initializeBattlefield(
                rows = event.rows,
                columns = event.columns,
                tiles = event.tiles,
            )
        }
}
