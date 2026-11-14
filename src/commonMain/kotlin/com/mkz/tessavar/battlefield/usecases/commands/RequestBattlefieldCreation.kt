package com.mkz.tessavar.battlefield.usecases.commands

import com.mkz.tessavar.battlefield.usecases.services.BattlefieldCreation
import com.mkz.tessavar.shared.domain.EventBus

class RequestBattlefieldCreation(
    private val battlefieldCreation: BattlefieldCreation,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        rows: Int,
        columns: Int,
        tiles: List<List<String>>,
    ) {
        eventBus.publish(battlefieldCreation.create(rows = rows, columns = columns, tiles = tiles))
    }
}
